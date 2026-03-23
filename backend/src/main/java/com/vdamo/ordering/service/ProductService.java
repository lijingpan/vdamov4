package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.common.exception.BadRequestException;
import com.vdamo.ordering.common.exception.NotFoundException;
import com.vdamo.ordering.common.support.IdGenerator;
import com.vdamo.ordering.entity.ProductAttrEntity;
import com.vdamo.ordering.entity.ProductAttrValueEntity;
import com.vdamo.ordering.entity.ProductCategoryEntity;
import com.vdamo.ordering.entity.ProductEntity;
import com.vdamo.ordering.entity.ProductSkuEntity;
import com.vdamo.ordering.entity.ProductSpecEntity;
import com.vdamo.ordering.entity.ProductSpecValueEntity;
import com.vdamo.ordering.entity.OrderItemEntity;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.mapper.OrderItemMapper;
import com.vdamo.ordering.mapper.ProductAttrMapper;
import com.vdamo.ordering.mapper.ProductAttrValueMapper;
import com.vdamo.ordering.mapper.ProductCategoryMapper;
import com.vdamo.ordering.mapper.ProductMapper;
import com.vdamo.ordering.mapper.ProductSkuMapper;
import com.vdamo.ordering.mapper.ProductSpecMapper;
import com.vdamo.ordering.mapper.ProductSpecValueMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.model.ProductDetailResponse;
import com.vdamo.ordering.model.ProductStatusUpdateRequest;
import com.vdamo.ordering.model.ProductSummary;
import com.vdamo.ordering.model.ProductUpsertRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProductService {

    private static final Set<String> PRODUCT_TYPES = Set.of("NORMAL", "WEIGHED", "ADD_ON", "SET_MEAL");
    private static final Set<String> SPEC_MODES = Set.of("SINGLE", "MULTI");

    private final ProductMapper productMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductSpecMapper productSpecMapper;
    private final ProductSpecValueMapper productSpecValueMapper;
    private final ProductAttrMapper productAttrMapper;
    private final ProductAttrValueMapper productAttrValueMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final StoreMapper storeMapper;
    private final PermissionService permissionService;
    private final IdGenerator idGenerator;

    public ProductService(
            ProductMapper productMapper,
            OrderItemMapper orderItemMapper,
            ProductSkuMapper productSkuMapper,
            ProductSpecMapper productSpecMapper,
            ProductSpecValueMapper productSpecValueMapper,
            ProductAttrMapper productAttrMapper,
            ProductAttrValueMapper productAttrValueMapper,
            ProductCategoryMapper productCategoryMapper,
            StoreMapper storeMapper,
            PermissionService permissionService,
            IdGenerator idGenerator
    ) {
        this.productMapper = productMapper;
        this.orderItemMapper = orderItemMapper;
        this.productSkuMapper = productSkuMapper;
        this.productSpecMapper = productSpecMapper;
        this.productSpecValueMapper = productSpecValueMapper;
        this.productAttrMapper = productAttrMapper;
        this.productAttrValueMapper = productAttrValueMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.storeMapper = storeMapper;
        this.permissionService = permissionService;
        this.idGenerator = idGenerator;
    }

    public List<ProductSummary> list(Long storeId, Boolean active, String categoryCode, String keyword) {
        List<Long> storeScope = permissionService.currentStoreIds();
        if (storeId != null) {
            permissionService.assertStoreAccess(storeId);
        }

        LambdaQueryWrapper<ProductEntity> wrapper = new LambdaQueryWrapper<ProductEntity>()
                .in(ProductEntity::getStoreId, storeScope)
                .orderByAsc(ProductEntity::getStoreId)
                .orderByAsc(ProductEntity::getCategoryCode)
                .orderByAsc(ProductEntity::getName);
        if (storeId != null) {
            wrapper.eq(ProductEntity::getStoreId, storeId);
        }
        if (active != null) {
            wrapper.eq(ProductEntity::getActive, active);
        }
        if (StringUtils.hasText(categoryCode)) {
            wrapper.eq(ProductEntity::getCategoryCode, categoryCode.trim().toUpperCase(Locale.ROOT));
        }
        if (StringUtils.hasText(keyword)) {
            String keywordValue = keyword.trim();
            wrapper.and(q -> q.like(ProductEntity::getName, keywordValue)
                    .or()
                    .like(ProductEntity::getCode, keywordValue)
                    .or()
                    .like(ProductEntity::getCategoryCode, keywordValue));
        }

        List<ProductEntity> products = productMapper.selectList(wrapper);
        Map<Long, String> storeNameMap = loadStoreNameMap(storeScope);
        Map<Long, ProductSkuMetrics> skuMetricsMap = loadSkuMetrics(products.stream().map(ProductEntity::getId).toList());
        return products.stream()
                .map(entity -> toSummary(entity, storeNameMap, skuMetricsMap.get(entity.getId())))
                .toList();
    }

    public ProductDetailResponse getDetail(Long id) {
        ProductEntity entity = requireProduct(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        Map<Long, String> storeNameMap = loadStoreNameMap(List.of(entity.getStoreId()));
        return toDetail(entity, storeNameMap.getOrDefault(entity.getStoreId(), ""));
    }

    @Transactional
    public ProductSummary create(ProductUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());

        PreparedProductPayload payload = preparePayload(request);
        ProductEntity entity = new ProductEntity();
        entity.setId(idGenerator.nextId());
        applyProductValues(entity, payload);
        productMapper.insert(entity);
        replaceChildren(entity.getId(), entity.getStoreId(), payload);
        return getSummary(entity.getId());
    }

    @Transactional
    public ProductSummary update(Long id, ProductUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());

        PreparedProductPayload payload = preparePayload(request);
        ProductEntity entity = requireProduct(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        applyProductValues(entity, payload);
        productMapper.updateById(entity);
        replaceChildren(entity.getId(), entity.getStoreId(), payload);
        return getSummary(entity.getId());
    }

    public ProductSummary updateStatus(Long id, ProductStatusUpdateRequest request) {
        ProductEntity entity = requireProduct(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        entity.setActive(request.active());
        entity.setUpdater(permissionService.currentUser().username());
        productMapper.updateById(entity);
        return getSummary(id);
    }

    @Transactional
    public void delete(Long id) {
        ProductEntity entity = requireProduct(id);
        permissionService.assertStoreAccess(entity.getStoreId());

        Long orderItemCount = orderItemMapper.selectCount(
                new LambdaQueryWrapper<OrderItemEntity>().eq(OrderItemEntity::getProductId, id));
        if (orderItemCount != null && orderItemCount > 0) {
            throw new BadRequestException("Product is referenced by orders and cannot be deleted");
        }

        productSpecValueMapper.delete(new LambdaQueryWrapper<ProductSpecValueEntity>()
                .eq(ProductSpecValueEntity::getProductId, id));
        productSpecMapper.delete(new LambdaQueryWrapper<ProductSpecEntity>()
                .eq(ProductSpecEntity::getProductId, id));
        productAttrValueMapper.delete(new LambdaQueryWrapper<ProductAttrValueEntity>()
                .eq(ProductAttrValueEntity::getProductId, id));
        productAttrMapper.delete(new LambdaQueryWrapper<ProductAttrEntity>()
                .eq(ProductAttrEntity::getProductId, id));
        productSkuMapper.delete(new LambdaQueryWrapper<ProductSkuEntity>()
                .eq(ProductSkuEntity::getProductId, id));
        productMapper.deleteById(entity.getId());
    }

    private void applyProductValues(ProductEntity entity, PreparedProductPayload payload) {
        validateProductUnique(entity.getId(), payload.storeId(), payload.code());
        requireCategory(payload.storeId(), payload.categoryCode());

        entity.setStoreId(payload.storeId());
        entity.setName(payload.name());
        entity.setCode(payload.code());
        entity.setCategoryCode(payload.categoryCode());
        entity.setProductType(payload.productType());
        entity.setSpecMode(payload.specMode());
        entity.setDescription(payload.description());
        entity.setAttrEnabled(payload.attrEnabled());
        entity.setMaterialEnabled(payload.materialEnabled());
        entity.setWeighedEnabled(payload.weighedEnabled());
        entity.setPriceInCent(deriveDisplayPrice(payload.skus()));
        entity.setActive(payload.active());
        entity.setUpdater(permissionService.currentUser().username());
    }

    private void validateSpecs(List<ProductUpsertRequest.SpecItem> specs) {
        Set<String> specNames = new HashSet<>();
        for (ProductUpsertRequest.SpecItem spec : specs) {
            String specName = requireText(spec.name(), "Spec name is required");
            if (!specNames.add(specName.toUpperCase(Locale.ROOT))) {
                throw new BadRequestException("Duplicate spec name: " + specName);
            }
            if (spec.values().isEmpty()) {
                throw new BadRequestException("Each spec must contain at least one value");
            }
            Set<String> valueNames = new HashSet<>();
            for (ProductUpsertRequest.SpecValueItem value : spec.values()) {
                String valueName = requireText(value.name(), "Spec value name is required");
                if (!valueNames.add(valueName.toUpperCase(Locale.ROOT))) {
                    throw new BadRequestException("Duplicate spec value: " + valueName);
                }
            }
        }
    }

    private void validateSkus(List<ProductUpsertRequest.SkuItem> skus) {
        if (skus.isEmpty()) {
            throw new BadRequestException("At least one SKU is required");
        }
        Set<String> skuKeys = new HashSet<>();
        for (ProductUpsertRequest.SkuItem sku : skus) {
            if (sku.priceInCent() == null || sku.priceInCent() < 0) {
                throw new BadRequestException("SKU price must be greater than or equal to 0");
            }
            if (sku.linePriceInCent() != null && sku.linePriceInCent() < 0) {
                throw new BadRequestException("SKU line price must be greater than or equal to 0");
            }
            if (sku.costPriceInCent() != null && sku.costPriceInCent() < 0) {
                throw new BadRequestException("SKU cost price must be greater than or equal to 0");
            }
            if (sku.boxFeeInCent() != null && sku.boxFeeInCent() < 0) {
                throw new BadRequestException("SKU box fee must be greater than or equal to 0");
            }
            if (sku.stockQty() != null && sku.stockQty() < 0) {
                throw new BadRequestException("SKU stock must be greater than or equal to 0");
            }
            if (sku.weightUnitGram() != null && sku.weightUnitGram() <= 0) {
                throw new BadRequestException("SKU weight unit must be greater than 0");
            }

            String specKey = StringUtils.hasText(sku.specKey()) ? sku.specKey().trim() : "DEFAULT";
            if (!skuKeys.add(specKey)) {
                throw new BadRequestException("Duplicate SKU spec key: " + specKey);
            }
        }
    }

    private void validateAttrs(List<ProductUpsertRequest.AttrItem> attrs) {
        Set<String> attrNames = new HashSet<>();
        for (ProductUpsertRequest.AttrItem attr : attrs) {
            String attrName = requireText(attr.name(), "Attribute name is required");
            if (!attrNames.add(attrName.toUpperCase(Locale.ROOT))) {
                throw new BadRequestException("Duplicate attribute name: " + attrName);
            }
            int defaultCount = 0;
            Set<String> valueNames = new HashSet<>();
            for (ProductUpsertRequest.AttrValueItem value : attr.values()) {
                String valueName = requireText(value.name(), "Attribute value name is required");
                if (!valueNames.add(valueName.toUpperCase(Locale.ROOT))) {
                    throw new BadRequestException("Duplicate attribute value: " + valueName);
                }
                if (Boolean.TRUE.equals(value.isDefault())) {
                    defaultCount++;
                }
            }
            if (defaultCount > 1) {
                throw new BadRequestException("Attribute can only have one default value");
            }
        }
    }

    private PreparedProductPayload preparePayload(ProductUpsertRequest request) {
        List<ProductUpsertRequest.SpecItem> specs = resolveSpecs(request);
        List<ProductUpsertRequest.AttrItem> attrs = request.attrs();

        String productType = resolveProductType(request.productType());
        String specMode = resolveSpecMode(request.specMode(), specs);
        int requestPrice = normalizeRequestPrice(request.priceInCent());
        List<ProductUpsertRequest.SkuItem> skus = resolveSkus(specs, request.skus(), specMode, requestPrice);

        validateSpecs(specs);
        validateSkus(skus);
        validateAttrs(attrs);

        return new PreparedProductPayload(
                request.storeId(),
                request.name().trim(),
                request.code().trim(),
                request.categoryCode().trim().toUpperCase(Locale.ROOT),
                productType,
                specMode,
                StringUtils.hasText(request.description()) ? request.description().trim() : null,
                request.attrEnabled() == null ? !attrs.isEmpty() : request.attrEnabled(),
                Boolean.TRUE.equals(request.materialEnabled()),
                Boolean.TRUE.equals(request.weighedEnabled()),
                request.active(),
                specs,
                skus,
                attrs
        );
    }

    private List<ProductUpsertRequest.SpecItem> resolveSpecs(ProductUpsertRequest request) {
        List<ProductUpsertRequest.SpecItem> specs = request.specs();
        if (!request.rules().isEmpty()) {
            specs = request.rules().stream()
                    .map(rule -> new ProductUpsertRequest.SpecItem(
                            rule.name(),
                            rule.sortOrder(),
                            rule.values().stream()
                                    .map(value -> new ProductUpsertRequest.SpecValueItem(value.name(), value.sortOrder()))
                                    .toList()))
                    .toList();
        }
        return specs;
    }

    private String resolveProductType(String value) {
        if (!StringUtils.hasText(value)) {
            return "NORMAL";
        }
        return normalizeProductType(value);
    }

    private String resolveSpecMode(String specMode, List<ProductUpsertRequest.SpecItem> specs) {
        boolean hasRules = !specs.isEmpty();
        if (!StringUtils.hasText(specMode)) {
            return hasRules ? "MULTI" : "SINGLE";
        }
        String normalized = normalizeSpecMode(specMode);
        if (hasRules && "SINGLE".equals(normalized)) {
            return "MULTI";
        }
        if (!hasRules && "MULTI".equals(normalized)) {
            return "SINGLE";
        }
        return normalized;
    }

    private int normalizeRequestPrice(Integer priceInCent) {
        if (priceInCent == null) {
            return 0;
        }
        if (priceInCent < 0) {
            throw new BadRequestException("Product price must be greater than or equal to 0");
        }
        return priceInCent;
    }

    private List<ProductUpsertRequest.SkuItem> resolveSkus(
            List<ProductUpsertRequest.SpecItem> specs,
            List<ProductUpsertRequest.SkuItem> providedSkus,
            String specMode,
            int requestPriceInCent
    ) {
        if (!providedSkus.isEmpty()) {
            List<ProductUpsertRequest.SkuItem> normalizedSkus = new ArrayList<>();
            for (int index = 0; index < providedSkus.size(); index++) {
                ProductUpsertRequest.SkuItem item = providedSkus.get(index);
                int price = item.priceInCent() == null ? requestPriceInCent : item.priceInCent();
                if (price < 0) {
                    throw new BadRequestException("SKU price must be greater than or equal to 0");
                }
                String defaultSpecKey = "SINGLE".equals(specMode) ? "DEFAULT" : "AUTO_" + (index + 1);
                String defaultSpecName = "SINGLE".equals(specMode) ? "Default" : "Spec " + (index + 1);
                normalizedSkus.add(new ProductUpsertRequest.SkuItem(
                        StringUtils.hasText(item.specKey()) ? item.specKey().trim() : defaultSpecKey,
                        StringUtils.hasText(item.specName()) ? item.specName().trim() : defaultSpecName,
                        StringUtils.hasText(item.skuCode()) ? item.skuCode().trim() : null,
                        StringUtils.hasText(item.barcode()) ? item.barcode().trim() : null,
                        price,
                        item.linePriceInCent() == null ? 0 : item.linePriceInCent(),
                        item.costPriceInCent() == null ? 0 : item.costPriceInCent(),
                        item.boxFeeInCent() == null ? 0 : item.boxFeeInCent(),
                        item.stockQty() == null ? 0 : item.stockQty(),
                        Boolean.TRUE.equals(item.autoReplenish()),
                        item.weightUnitGram(),
                        item.sortOrder() == null ? index + 1 : item.sortOrder(),
                        item.active() == null || item.active()
                ));
            }
            return normalizedSkus;
        }

        if (specs.isEmpty()) {
            return List.of(new ProductUpsertRequest.SkuItem(
                    "DEFAULT",
                    "Default",
                    null,
                    null,
                    requestPriceInCent,
                    0,
                    0,
                    0,
                    0,
                    false,
                    null,
                    1,
                    true
            ));
        }

        List<SpecCombination> combinations = buildSpecCombinations(specs);
        List<ProductUpsertRequest.SkuItem> generated = new ArrayList<>();
        for (int index = 0; index < combinations.size(); index++) {
            SpecCombination combination = combinations.get(index);
            generated.add(new ProductUpsertRequest.SkuItem(
                    combination.specKey(),
                    combination.specName(),
                    null,
                    null,
                    requestPriceInCent,
                    0,
                    0,
                    0,
                    0,
                    false,
                    null,
                    index + 1,
                    true
            ));
        }
        return generated;
    }

    private List<SpecCombination> buildSpecCombinations(List<ProductUpsertRequest.SpecItem> specs) {
        List<SpecCombination> combinations = new ArrayList<>();
        combinations.add(new SpecCombination("", ""));

        for (ProductUpsertRequest.SpecItem spec : specs) {
            String specName = requireText(spec.name(), "Spec name is required");
            List<ProductUpsertRequest.SpecValueItem> values = spec.values();
            if (values.isEmpty()) {
                throw new BadRequestException("Each spec must contain at least one value");
            }

            List<SpecCombination> next = new ArrayList<>();
            for (SpecCombination base : combinations) {
                for (ProductUpsertRequest.SpecValueItem value : values) {
                    String valueName = requireText(value.name(), "Spec value name is required");
                    String nextKeyPart = normalizeSpecKeyPart(specName) + ":" + valueName;
                    String nextSpecKey = StringUtils.hasText(base.specKey())
                            ? base.specKey() + "|" + nextKeyPart
                            : nextKeyPart;
                    String nextSpecName = StringUtils.hasText(base.specName())
                            ? base.specName() + " / " + valueName
                            : valueName;
                    next.add(new SpecCombination(nextSpecKey, nextSpecName));
                }
            }
            combinations = next;
        }
        return combinations;
    }

    private String normalizeSpecKeyPart(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "_");
    }

    private int deriveDisplayPrice(List<ProductUpsertRequest.SkuItem> skus) {
        return skus.stream()
                .map(ProductUpsertRequest.SkuItem::priceInCent)
                .filter(value -> value != null && value >= 0)
                .min(Comparator.naturalOrder())
                .orElse(0);
    }

    private String normalizeProductType(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!PRODUCT_TYPES.contains(normalized)) {
            throw new BadRequestException("Unsupported product type");
        }
        return normalized;
    }

    private String normalizeSpecMode(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!SPEC_MODES.contains(normalized)) {
            throw new BadRequestException("Unsupported product spec mode");
        }
        return normalized;
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private void replaceChildren(Long productId, Long storeId, PreparedProductPayload payload) {
        productSpecValueMapper.delete(new LambdaQueryWrapper<ProductSpecValueEntity>()
                .eq(ProductSpecValueEntity::getProductId, productId));
        productSpecMapper.delete(new LambdaQueryWrapper<ProductSpecEntity>()
                .eq(ProductSpecEntity::getProductId, productId));
        productAttrValueMapper.delete(new LambdaQueryWrapper<ProductAttrValueEntity>()
                .eq(ProductAttrValueEntity::getProductId, productId));
        productAttrMapper.delete(new LambdaQueryWrapper<ProductAttrEntity>()
                .eq(ProductAttrEntity::getProductId, productId));
        productSkuMapper.delete(new LambdaQueryWrapper<ProductSkuEntity>()
                .eq(ProductSkuEntity::getProductId, productId));

        persistSpecs(productId, storeId, payload.specs());
        persistAttrs(productId, storeId, payload.attrs());
        persistSkus(productId, storeId, payload.skus());
    }

    private void persistSpecs(Long productId, Long storeId, List<ProductUpsertRequest.SpecItem> specs) {
        String username = permissionService.currentUser().username();
        for (int specIndex = 0; specIndex < specs.size(); specIndex++) {
            ProductUpsertRequest.SpecItem item = specs.get(specIndex);
            ProductSpecEntity specEntity = new ProductSpecEntity();
            specEntity.setId(idGenerator.nextId());
            specEntity.setProductId(productId);
            specEntity.setStoreId(storeId);
            specEntity.setName(item.name().trim());
            specEntity.setSortOrder(item.sortOrder() == null ? specIndex + 1 : item.sortOrder());
            specEntity.setUpdater(username);
            productSpecMapper.insert(specEntity);

            for (int valueIndex = 0; valueIndex < item.values().size(); valueIndex++) {
                ProductUpsertRequest.SpecValueItem value = item.values().get(valueIndex);
                ProductSpecValueEntity valueEntity = new ProductSpecValueEntity();
                valueEntity.setId(idGenerator.nextId());
                valueEntity.setProductSpecId(specEntity.getId());
                valueEntity.setProductId(productId);
                valueEntity.setStoreId(storeId);
                valueEntity.setName(value.name().trim());
                valueEntity.setSortOrder(value.sortOrder() == null ? valueIndex + 1 : value.sortOrder());
                valueEntity.setUpdater(username);
                productSpecValueMapper.insert(valueEntity);
            }
        }
    }

    private void persistAttrs(Long productId, Long storeId, List<ProductUpsertRequest.AttrItem> attrs) {
        String username = permissionService.currentUser().username();
        for (int attrIndex = 0; attrIndex < attrs.size(); attrIndex++) {
            ProductUpsertRequest.AttrItem item = attrs.get(attrIndex);
            ProductAttrEntity attrEntity = new ProductAttrEntity();
            attrEntity.setId(idGenerator.nextId());
            attrEntity.setProductId(productId);
            attrEntity.setStoreId(storeId);
            attrEntity.setName(item.name().trim());
            attrEntity.setSortOrder(item.sortOrder() == null ? attrIndex + 1 : item.sortOrder());
            attrEntity.setUpdater(username);
            productAttrMapper.insert(attrEntity);

            for (int valueIndex = 0; valueIndex < item.values().size(); valueIndex++) {
                ProductUpsertRequest.AttrValueItem value = item.values().get(valueIndex);
                ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
                valueEntity.setId(idGenerator.nextId());
                valueEntity.setProductAttrId(attrEntity.getId());
                valueEntity.setProductId(productId);
                valueEntity.setStoreId(storeId);
                valueEntity.setName(value.name().trim());
                valueEntity.setIsDefault(Boolean.TRUE.equals(value.isDefault()));
                valueEntity.setSortOrder(value.sortOrder() == null ? valueIndex + 1 : value.sortOrder());
                valueEntity.setUpdater(username);
                productAttrValueMapper.insert(valueEntity);
            }
        }
    }

    private void persistSkus(Long productId, Long storeId, List<ProductUpsertRequest.SkuItem> skus) {
        String username = permissionService.currentUser().username();
        for (int index = 0; index < skus.size(); index++) {
            ProductUpsertRequest.SkuItem item = skus.get(index);
            ProductSkuEntity entity = new ProductSkuEntity();
            entity.setId(idGenerator.nextId());
            entity.setProductId(productId);
            entity.setStoreId(storeId);
            entity.setSkuCode(StringUtils.hasText(item.skuCode()) ? item.skuCode().trim() : null);
            entity.setBarcode(StringUtils.hasText(item.barcode()) ? item.barcode().trim() : null);
            entity.setSpecKey(StringUtils.hasText(item.specKey()) ? item.specKey().trim() : "DEFAULT");
            entity.setSpecName(StringUtils.hasText(item.specName()) ? item.specName().trim() : "Default");
            entity.setPriceInCent(item.priceInCent());
            entity.setLinePriceInCent(item.linePriceInCent() == null ? 0 : item.linePriceInCent());
            entity.setCostPriceInCent(item.costPriceInCent() == null ? 0 : item.costPriceInCent());
            entity.setBoxFeeInCent(item.boxFeeInCent() == null ? 0 : item.boxFeeInCent());
            entity.setStockQty(item.stockQty() == null ? 0 : item.stockQty());
            entity.setAutoReplenish(Boolean.TRUE.equals(item.autoReplenish()));
            entity.setWeightUnitGram(item.weightUnitGram());
            entity.setSortOrder(item.sortOrder() == null ? index + 1 : item.sortOrder());
            entity.setActive(item.active() == null || item.active());
            entity.setUpdater(username);
            productSkuMapper.insert(entity);
        }
    }

    private void validateProductUnique(Long currentId, Long storeId, String code) {
        List<ProductEntity> duplicates = productMapper.selectList(
                new LambdaQueryWrapper<ProductEntity>()
                        .eq(ProductEntity::getStoreId, storeId)
                        .eq(ProductEntity::getCode, code));
        boolean exists = duplicates.stream().anyMatch(item -> !item.getId().equals(currentId));
        if (exists) {
            throw new BadRequestException("Product code already exists in this store");
        }
    }

    private void requireCategory(Long storeId, String categoryCode) {
        boolean exists = productCategoryMapper.selectCount(
                new LambdaQueryWrapper<ProductCategoryEntity>()
                        .eq(ProductCategoryEntity::getStoreId, storeId)
                        .eq(ProductCategoryEntity::getCategoryCode, categoryCode)) > 0;
        if (!exists) {
            throw new BadRequestException("Product category does not exist");
        }
    }

    private void requireStore(Long storeId) {
        if (storeMapper.selectById(storeId) == null) {
            throw new NotFoundException("Store not found");
        }
    }

    private ProductEntity requireProduct(Long id) {
        ProductEntity entity = productMapper.selectById(id);
        if (entity == null) {
            throw new NotFoundException("Product not found");
        }
        return entity;
    }

    private ProductSummary getSummary(Long id) {
        ProductEntity entity = requireProduct(id);
        Map<Long, String> storeNameMap = loadStoreNameMap(List.of(entity.getStoreId()));
        ProductSkuMetrics metrics = loadSkuMetrics(List.of(id)).get(id);
        return toSummary(entity, storeNameMap, metrics);
    }

    private Map<Long, ProductSkuMetrics> loadSkuMetrics(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<ProductSkuEntity>> skuGroups = productSkuMapper.selectList(
                        new LambdaQueryWrapper<ProductSkuEntity>()
                                .in(ProductSkuEntity::getProductId, productIds))
                .stream()
                .collect(Collectors.groupingBy(ProductSkuEntity::getProductId));

        Map<Long, ProductSkuMetrics> result = new HashMap<>();
        skuGroups.forEach((productId, skuList) -> {
            int minPrice = skuList.stream()
                    .map(ProductSkuEntity::getPriceInCent)
                    .filter(value -> value != null)
                    .min(Comparator.naturalOrder())
                    .orElse(0);
            int maxPrice = skuList.stream()
                    .map(ProductSkuEntity::getPriceInCent)
                    .filter(value -> value != null)
                    .max(Comparator.naturalOrder())
                    .orElse(minPrice);
            result.put(productId, new ProductSkuMetrics(minPrice, maxPrice, skuList.size()));
        });
        return result;
    }

    private Map<Long, String> loadStoreNameMap(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Map.of();
        }
        return storeMapper.selectList(new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeIds))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left));
    }

    private ProductSummary toSummary(
            ProductEntity entity,
            Map<Long, String> storeNameMap,
            ProductSkuMetrics metrics
    ) {
        int minPrice = metrics == null ? (entity.getPriceInCent() == null ? 0 : entity.getPriceInCent()) : metrics.minPriceInCent();
        int maxPrice = metrics == null ? minPrice : metrics.maxPriceInCent();
        int skuCount = metrics == null ? 0 : metrics.skuCount();
        return new ProductSummary(
                entity.getId(),
                entity.getStoreId(),
                storeNameMap.getOrDefault(entity.getStoreId(), ""),
                entity.getName(),
                entity.getCode(),
                entity.getCategoryCode(),
                entity.getProductType(),
                entity.getSpecMode(),
                minPrice,
                maxPrice,
                skuCount,
                Boolean.TRUE.equals(entity.getAttrEnabled()),
                Boolean.TRUE.equals(entity.getWeighedEnabled()),
                Boolean.TRUE.equals(entity.getActive()));
    }

    private ProductDetailResponse toDetail(ProductEntity entity, String storeName) {
        List<ProductSpecEntity> specEntities = productSpecMapper.selectList(
                new LambdaQueryWrapper<ProductSpecEntity>()
                        .eq(ProductSpecEntity::getProductId, entity.getId())
                        .orderByAsc(ProductSpecEntity::getSortOrder, ProductSpecEntity::getId));
        List<ProductSpecValueEntity> specValueEntities = productSpecValueMapper.selectList(
                new LambdaQueryWrapper<ProductSpecValueEntity>()
                        .eq(ProductSpecValueEntity::getProductId, entity.getId())
                        .orderByAsc(ProductSpecValueEntity::getSortOrder, ProductSpecValueEntity::getId));
        Map<Long, List<ProductSpecValueEntity>> specValuesBySpecId = specValueEntities.stream()
                .collect(Collectors.groupingBy(
                        ProductSpecValueEntity::getProductSpecId,
                        LinkedHashMap::new,
                        Collectors.toCollection(ArrayList::new)));

        List<ProductAttrEntity> attrEntities = productAttrMapper.selectList(
                new LambdaQueryWrapper<ProductAttrEntity>()
                        .eq(ProductAttrEntity::getProductId, entity.getId())
                        .orderByAsc(ProductAttrEntity::getSortOrder, ProductAttrEntity::getId));
        List<ProductAttrValueEntity> attrValueEntities = productAttrValueMapper.selectList(
                new LambdaQueryWrapper<ProductAttrValueEntity>()
                        .eq(ProductAttrValueEntity::getProductId, entity.getId())
                        .orderByAsc(ProductAttrValueEntity::getSortOrder, ProductAttrValueEntity::getId));
        Map<Long, List<ProductAttrValueEntity>> attrValuesByAttrId = attrValueEntities.stream()
                .collect(Collectors.groupingBy(
                        ProductAttrValueEntity::getProductAttrId,
                        LinkedHashMap::new,
                        Collectors.toCollection(ArrayList::new)));

        List<ProductSkuEntity> skuEntities = productSkuMapper.selectList(
                new LambdaQueryWrapper<ProductSkuEntity>()
                        .eq(ProductSkuEntity::getProductId, entity.getId())
                        .orderByAsc(ProductSkuEntity::getSortOrder, ProductSkuEntity::getId));

        List<ProductDetailResponse.SpecItem> specs = specEntities.stream()
                .map(spec -> new ProductDetailResponse.SpecItem(
                        spec.getId(),
                        spec.getName(),
                        spec.getSortOrder() == null ? 0 : spec.getSortOrder(),
                        specValuesBySpecId.getOrDefault(spec.getId(), List.of()).stream()
                                .map(value -> new ProductDetailResponse.SpecValueItem(
                                        value.getId(),
                                        value.getName(),
                                        value.getSortOrder() == null ? 0 : value.getSortOrder()))
                                .toList()))
                .toList();

        List<ProductDetailResponse.AttrItem> attrs = attrEntities.stream()
                .map(attr -> new ProductDetailResponse.AttrItem(
                        attr.getId(),
                        attr.getName(),
                        attr.getSortOrder() == null ? 0 : attr.getSortOrder(),
                        attrValuesByAttrId.getOrDefault(attr.getId(), List.of()).stream()
                                .map(value -> new ProductDetailResponse.AttrValueItem(
                                        value.getId(),
                                        value.getName(),
                                        Boolean.TRUE.equals(value.getIsDefault()),
                                        value.getSortOrder() == null ? 0 : value.getSortOrder()))
                                .toList()))
                .toList();

        List<ProductDetailResponse.SkuItem> skus = skuEntities.stream()
                .map(sku -> new ProductDetailResponse.SkuItem(
                        sku.getId(),
                        sku.getSpecKey(),
                        sku.getSpecName(),
                        sku.getSkuCode(),
                        sku.getBarcode(),
                        sku.getPriceInCent() == null ? 0 : sku.getPriceInCent(),
                        sku.getLinePriceInCent() == null ? 0 : sku.getLinePriceInCent(),
                        sku.getCostPriceInCent() == null ? 0 : sku.getCostPriceInCent(),
                        sku.getBoxFeeInCent() == null ? 0 : sku.getBoxFeeInCent(),
                        sku.getStockQty() == null ? 0 : sku.getStockQty(),
                        Boolean.TRUE.equals(sku.getAutoReplenish()),
                        sku.getWeightUnitGram(),
                        sku.getSortOrder() == null ? 0 : sku.getSortOrder(),
                        Boolean.TRUE.equals(sku.getActive())))
                .toList();

        return new ProductDetailResponse(
                entity.getId(),
                entity.getStoreId(),
                storeName,
                entity.getName(),
                entity.getCode(),
                entity.getCategoryCode(),
                entity.getProductType(),
                entity.getSpecMode(),
                entity.getDescription(),
                Boolean.TRUE.equals(entity.getAttrEnabled()),
                Boolean.TRUE.equals(entity.getMaterialEnabled()),
                Boolean.TRUE.equals(entity.getWeighedEnabled()),
                Boolean.TRUE.equals(entity.getActive()),
                specs,
                skus,
                attrs);
    }

    private record ProductSkuMetrics(
            int minPriceInCent,
            int maxPriceInCent,
            int skuCount
    ) {
    }

    private record SpecCombination(
            String specKey,
            String specName
    ) {
    }

    private record PreparedProductPayload(
            Long storeId,
            String name,
            String code,
            String categoryCode,
            String productType,
            String specMode,
            String description,
            boolean attrEnabled,
            boolean materialEnabled,
            boolean weighedEnabled,
            boolean active,
            List<ProductUpsertRequest.SpecItem> specs,
            List<ProductUpsertRequest.SkuItem> skus,
            List<ProductUpsertRequest.AttrItem> attrs
    ) {
    }
}

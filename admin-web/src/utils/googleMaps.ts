const GOOGLE_MAPS_API_KEY = 'AIzaSyCpDS9CicN6wlBAyw5UsyNrvy6Z3K_vuSM';
const GOOGLE_MAPS_LOADER_ATTR = 'data-google-maps-loader';

let googleMapsPromise: Promise<any> | null = null;

function buildGoogleMapsSrc(): string {
  const params = new URLSearchParams({
    key: GOOGLE_MAPS_API_KEY,
    libraries: 'places',
  });
  return `https://maps.googleapis.com/maps/api/js?${params.toString()}`;
}

function isGoogleMapsReady(): boolean {
  return Boolean((window as any).google?.maps);
}

export function loadGoogleMapsApi(): Promise<any> {
  if (isGoogleMapsReady()) {
    return Promise.resolve((window as any).google);
  }
  if (googleMapsPromise) {
    return googleMapsPromise;
  }

  googleMapsPromise = new Promise((resolve, reject) => {
    const complete = () => {
      if (isGoogleMapsReady()) {
        resolve((window as any).google);
        return;
      }
      googleMapsPromise = null;
      reject(new Error('Google Maps API not available.'));
    };

    const fail = () => {
      googleMapsPromise = null;
      reject(new Error('Failed to load Google Maps API.'));
    };

    const existing = document.querySelector<HTMLScriptElement>(`script[${GOOGLE_MAPS_LOADER_ATTR}="true"]`);
    if (existing) {
      existing.addEventListener('load', complete, { once: true });
      existing.addEventListener('error', fail, { once: true });
      return;
    }

    const script = document.createElement('script');
    script.src = buildGoogleMapsSrc();
    script.async = true;
    script.defer = true;
    script.setAttribute(GOOGLE_MAPS_LOADER_ATTR, 'true');
    script.addEventListener('load', complete, { once: true });
    script.addEventListener('error', fail, { once: true });
    document.head.appendChild(script);
  });

  return googleMapsPromise;
}

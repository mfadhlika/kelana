export type Visit = {
  name?: string;
  coordinates: L.LatLng;
  startAt: Date;
  endAt: Date;
  mode?: string[];
  distance?: number;
  address?: string;
};

import type { FeatureCollection, Point } from "geojson";

type PointProperties = {
    timestamp: string,
    altitude?: number,
    speed?: number,
    course?: number,
    courseAccuracy?: number,
    accuracy?: number,
    verticalAccuracy?: number,
    motions: string[],
    batteryState?: string,
    batteryLevel?: number,
    deviceId: string,
    ssid?: string,
    rawData: string,
    geocode?: FeatureCollection<Point, GeocodeProperties>
};

type LineStringProperties = {
    distance?: number,
    distanceUnit: string,
    speed?: number,
    speedUnit: string,
    startAt: Date,
    endAt: Date,
    motions: string[]
};

type TripProperties = {
    id: number,
    title: string,
    startAt: string,
    endAt: string,
    uuid: string,
    public: boolean,
    publicUrl?: string
}

type GeocodeProperties = {
    postcode: string,
    name: string,
    country: string,
    city: string,
    district: string,
    locality: string,
    street: string,
    state: string
}

type RegionProperties = {
    desc: string,
    rid?: string,
    beaconUUID?: string,
    beaconMajor?: number,
    beaconMinor?: number
}

export type { PointProperties, LineStringProperties, TripProperties, RegionProperties };

import { axiosInstance, handleResponse } from "@/lib/request";
import type { PlaceProperties } from "@/types/properties";
import type { Response } from "@/types/response";
import type { FeatureCollection, Point } from "geojson";

class PlaceService {
    findPlaces = async (lon: number, lat: number, rad?: number): Promise<Response<FeatureCollection<Point, PlaceProperties>>> => {
        const params = new URLSearchParams();
        params.set('lon', `${lon}`);
        params.set('lat', `${lat}`);
        if (rad) params.set('rad', `${rad}`);
        return await handleResponse(axiosInstance.get<Response<FeatureCollection<Point, PlaceProperties>>>(`v1/places?${params.toString()}`));
    }
}

export const placeService = new PlaceService();

import { axiosInstance, handleResponse } from "@/lib/request";
import type { TripProperties } from "@/types/properties";
import type { Trip } from "@/types/requests/trip";
import { type Response } from "@/types/response";
import type { Feature, FeatureCollection, MultiLineString, Point } from "geojson";

class TripSerice {
    fetchTrips = async (): Promise<Response<FeatureCollection<MultiLineString, TripProperties>>> => {
        return await handleResponse(axiosInstance
            .get<Response<FeatureCollection<MultiLineString, TripProperties>>>("v1/trips"));
    }

    fetchTrip = async (uuid: string): Promise<Response<Feature<Point, TripProperties>>> => {
        return await handleResponse(axiosInstance
            .get<Response<Feature<Point, TripProperties>>>(`v1/trips/${uuid}`));
    }

    createTrip = async (trip: Trip): Promise<Response<Feature<MultiLineString, TripProperties>>> => {
        return await handleResponse(axiosInstance
            .post(`v1/trips`, trip));
    }

    deleteTrip = async (id: number): Promise<Response> => {
        return await handleResponse(axiosInstance
            .delete<Response>(`v1/trips/${id}`));
    }
}

export const tripService = new TripSerice();

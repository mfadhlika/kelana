import { axiosInstance } from "@/lib/request";
import type { RegionProperties } from "@/types/properties";
import type { Response } from "@/types/response";
import type { FeatureCollection, LineString } from "geojson";

class RegionService {
    createRegions = async (payload: FeatureCollection): Promise<Response> => {
        return await axiosInstance.post(`v1/regions`, payload);
    }

    fetchRegions = async (): Promise<Response<FeatureCollection<LineString, RegionProperties>>> => {
        return await axiosInstance.get<Response<FeatureCollection<LineString, RegionProperties>>>(`v1/regions`).then(res => res.data);
    }
}

export const regionService = new RegionService();

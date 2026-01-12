import { axiosInstance } from "@/lib/request";
import type { RegionProperties } from "@/types/properties";
import type { Response } from "@/types/response";
import type { FeatureCollection, LineString } from "geojson";

class RegionService {
    fetchRegions = async (): Promise<Response<FeatureCollection<LineString, RegionProperties>>> => {
        return await axiosInstance.get<Response<FeatureCollection<LineString, RegionProperties>>>(`v1/regions`).then(res => res.data);
    }
}

export const regionService = new RegionService();

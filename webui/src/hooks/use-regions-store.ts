import { regionService } from "@/services/region-service";
import type { RegionProperties } from "@/types/properties";
import type { FeatureCollection, LineString } from "geojson";
import { create } from "zustand";
import * as turf from "@turf/turf";

type RegionStore = {
    data: FeatureCollection<LineString, RegionProperties>,
    isLoading: boolean,
    error?: { message: string },
    fetch: () => Promise<void>
}

const useRegionsStore = create<RegionStore>((set) => ({
    data: turf.featureCollection<LineString, RegionProperties>([]),
    isLoading: false,
    error: undefined,
    fetch: async () => {
        set({ isLoading: true });
        try {
            const res = await regionService.fetchRegions();
            set({ data: res.data, isLoading: false, error: undefined });
        } catch (err) {
            set({ isLoading: false, error: (err as { response: { data: { message: string } } }).response.data })
            throw err;
        }
    }
}));

export { type RegionStore, useRegionsStore };

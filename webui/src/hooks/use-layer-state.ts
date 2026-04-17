import type { Checked } from "@/types/checked";
import { create } from 'zustand';
import { createJSONStorage, persist } from 'zustand/middleware';

export type LayerState = React.ComponentProps<"div"> & {
    showLines: Checked,
    showPoints: Checked,
    showLastKnown: Checked,
    showMovingPoints: Checked,
    showTimeline: Checked,
    showRegions: Checked,
}

export const useLayerState = create<LayerState>()((
    persist(
        () => ({
            showLines: true as Checked,
            showPoints: true as Checked,
            showLastKnown: true as Checked,
            showMovingPoints: false as Checked,
            showTimeline: false as Checked,
            showRegions: true as Checked,
        }),
        {
            name: 'layer-storage',
            storage: createJSONStorage(() => localStorage)
        }
    )
));

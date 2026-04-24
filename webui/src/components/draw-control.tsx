import { useCallback, useEffect, useRef } from "react";
import { Button } from "@/components/ui/button";
import { ButtonGroup } from "@/components/ui/button-group";
import { Circle, Loader2Icon, Square } from "lucide-react";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { setDrawState, useDrawState } from "@/hooks/use-draw-state";
import L from "leaflet";
import { MapControl } from "./map-control";
import { useLeafletContext } from "@react-leaflet/core";
import { createRoot } from "react-dom/client";
import { regionFormSchema } from "@/types/schema/region";
import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import type { Region } from "@/types/requests/region";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "./ui/input";
import * as turf from "@turf/turf";
import type { RegionProperties } from "@/types/properties";
import { regionService } from "@/services/region-service";
import { toast } from "sonner";
import { useRegionsStore } from "@/hooks/use-regions-store";

const RegionEditPopup = ({ region, onCancel, onSubmit }: { region?: Region, onCancel: () => void, onSubmit: (region: Region) => Promise<void> }) => {
    const form = useForm<Region>({
        resolver: zodResolver(regionFormSchema),
        values: region,
    });

    return (
        <form onSubmit={form.handleSubmit(onSubmit)}>
            <FieldGroup className="gap-2">
                <Controller control={form.control} name="desc" render={({ field }) => (
                    <Field orientation="horizontal">
                        <FieldLabel className="w-1/2">Description</FieldLabel>
                        <Input type="text" {...field} />
                    </Field>
                )} />
                <Controller control={form.control} name="rid" render={({ field }) => (
                    <Field orientation="horizontal">
                        <FieldLabel className="w-1/2">Region ID</FieldLabel>
                        <Input type="text" {...field} />
                    </Field>
                )} />

                <Controller control={form.control} name="beaconUUID" render={({ field }) => (
                    <Field orientation="horizontal">
                        <FieldLabel className="w-1/2">Beacon UUID</FieldLabel>
                        <Input type="text" {...field} />
                    </Field>
                )} />
                <Controller control={form.control} name="beaconMajor" render={({ field }) => (
                    <Field orientation="horizontal">
                        <FieldLabel className="w-1/2">Beacon Major</FieldLabel>
                        <Input type="text" {...field} />
                    </Field>
                )} />
                <Controller control={form.control} name="beaconMinor" render={({ field }) => (
                    <Field orientation="horizontal">
                        <FieldLabel className="w-1/2">Beacon Minor</FieldLabel>
                        <Input type="text" {...field} />
                    </Field>
                )} />
                <Field orientation="horizontal" className="justify-end">
                    <Button type="button" variant="outline" disabled={form.formState.isSubmitting} onClick={onCancel}>
                        Cancel
                    </Button>
                    <Button type="submit" disabled={form.formState.isSubmitting}>
                        {form.formState.isSubmitting && <Loader2Icon className="animate-spin" />}
                        Save
                    </Button>
                </Field>
            </FieldGroup>
        </form>
    );
}

export const DrawControl = () => {
    const drawState = useDrawState();
    const activeMarkerRef = useRef<L.Layer>(null);
    const context = useLeafletContext();
    const fetchRegions = useRegionsStore((state) => state.fetch);

    const mapClickCallback = useCallback((e: L.LeafletMouseEvent) => {
        switch (drawState) {
            case "view":
                break;
            case "draw:square:start": {
                activeMarkerRef.current = L.rectangle(e.latlng.toBounds(0));
                context.layerContainer?.addLayer(activeMarkerRef.current);
                setDrawState("draw:square:resize");
                break;
            }
            case "draw:circle:start": {
                activeMarkerRef.current = L.circle([e.latlng.lat, e.latlng.lng], { radius: 0 });
                context.layerContainer?.addLayer(activeMarkerRef.current);
                setDrawState("draw:circle:resize");
                break;
            }
            case "draw:square:resize":
            case "draw:circle:resize":
                setDrawState("draw:edit");
                break;
            case "draw:end":
                break;
        }
    }, [context.layerContainer, drawState]);

    const mapMousemoveCallback = useCallback((e: L.LeafletMouseEvent) => {
        switch (drawState) {
            case "draw:square:resize":
                if (activeMarkerRef.current instanceof L.Rectangle) {
                    const center = activeMarkerRef.current.getCenter();
                    const distance = center.distanceTo(e.latlng);
                    activeMarkerRef.current.setBounds(center.toBounds(distance * 2));
                }
                break;
            case "draw:circle:resize":
                if (activeMarkerRef.current instanceof L.Circle) {
                    const radius = activeMarkerRef.current.getLatLng().distanceTo(e.latlng);
                    activeMarkerRef.current.setRadius(radius);
                }
                break;
            default:
                break;
        }
    }, [drawState]);

    useEffect(() => {
        context.map.addEventListener("click", mapClickCallback);

        context.map.addEventListener("mousemove", mapMousemoveCallback);

        return () => {
            context.map.removeEventListener("click", mapClickCallback);
            context.map.removeEventListener("mousemove", mapMousemoveCallback);
        }
    }, [context.map, mapClickCallback, mapMousemoveCallback]);

    useEffect(() => {
        switch (drawState) {
            case "view":
                break;
            case "draw:square:start":
            case "draw:circle:start":
                context.map.getContainer().classList.add("cursor-crosshair!");
                break;
            case "draw:square:resize":
            case "draw:circle:resize":
                break;
            case "draw:edit": {
                const div = document.createElement("div");
                div.className = "min-w-[300px]"
                const root = createRoot(div);
                root.render(<RegionEditPopup
                    onCancel={() => {
                        root.unmount();
                        setDrawState("draw:end");
                    }}
                    onSubmit={async (region: RegionProperties) => {
                        const props = {
                            desc: region.desc,
                            rid: region.rid,
                            beaconUUID: region.beaconUUID,
                            beaconMajor: region.beaconMajor,
                            beaconMinor: region.beaconMinor,
                        };
                        let feature;
                        if (activeMarkerRef.current instanceof L.Rectangle) {
                            const bounds = activeMarkerRef.current.getBounds();

                            feature = turf.polygon<RegionProperties>([
                                [
                                    [bounds.getNorthEast().lng, bounds.getNorthEast().lat],
                                    [bounds.getNorthWest().lng, bounds.getNorthWest().lat],
                                    [bounds.getSouthWest().lng, bounds.getSouthWest().lat],
                                    [bounds.getSouthEast().lng, bounds.getSouthEast().lat],
                                    [bounds.getNorthEast().lng, bounds.getNorthEast().lat],
                                ]
                            ], props);
                        } else if (activeMarkerRef.current instanceof L.Circle) {
                            const latlng = activeMarkerRef.current.getLatLng();
                            feature = turf.circle([latlng.lng, latlng.lat], activeMarkerRef.current.getRadius(), { units: "meters", properties: props });
                        } else {
                            return Promise.reject("unsupported region");
                        }

                        const payload = turf.featureCollection([feature]);
                        try {
                            await regionService.createRegions(payload);
                        } catch (err) {
                            toast.error(`Failed to create regions: ${(err as { message: string }).message}`);
                        } finally {
                            root.unmount();
                            setDrawState("draw:end");
                        }
                    }} />);
                activeMarkerRef.current?.bindPopup(div, { autoClose: false, closeOnClick: false, closeButton: false, closeOnEscapeKey: false }).openPopup();
                context.map.getContainer().classList.remove("cursor-crosshair!");
                break;
            }
            case "draw:end": {
                if (activeMarkerRef.current) {
                    activeMarkerRef.current.closePopup();
                    context.layerContainer?.removeLayer(activeMarkerRef.current);
                    activeMarkerRef.current = null;
                    fetchRegions();
                }
                setDrawState("view");
                break;
            }

        }
    }, [context.layerContainer, context.map, drawState, fetchRegions]);

    const buttons = [
        [
            {
                name: "square",
                icon: Square,
                tooltip: "Create a square region",
                onclick: () => setDrawState("draw:square:start")
            },
            {
                name: "circle",
                icon: Circle,
                tooltip: "Create a circle region",
                onclick: () => setDrawState("draw:circle:start")
            }
        ]
    ];

    return (
        <MapControl position='bottomright' disableClickPropagation disableScrollPropagation className='flex flex-col gap-2'>
            {buttons.map(buttons => <ButtonGroup orientation="vertical">{buttons.map(button =>
                <Tooltip>
                    <TooltipTrigger asChild>
                        <Button variant="outline" onClick={button.onclick}>
                            <button.icon />
                        </Button>
                    </TooltipTrigger>
                    <TooltipContent side="left">
                        <p>{button.tooltip}</p>
                    </TooltipContent>
                </Tooltip>
            )}</ButtonGroup>)}
        </MapControl>);
}

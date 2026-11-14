import { cn } from "@/lib/utils";
import { formatDate, formatDistanceStrict } from "date-fns";
import { CarFront, CircleDot } from "lucide-react";
import { MapControl } from "./map-control";
import { useIsMobile } from "@/hooks/use-mobile";
import type { Visit } from "@/types/visit";
import { useMap } from "react-leaflet";

export type TimelineProps = {
  visits: Visit[]
}

export function Timeline({
  visits
}: TimelineProps) {
  const isMobile = useIsMobile();
  const map = useMap();

  return (
    <MapControl position={isMobile ? "bottomright" : "topleft"} disableClickPropagation={true} disableScrollPropagation={true}>
        <div className="leaflet-touch bg-sidebar rounded-xl border border-gray-300 w-[calc(100vw-20px)] md:w-[20vw] max-h-[25vh] md:max-h-[calc(90vh)] overflow-y-auto flex flex-col p-2">
            {visits.map((cur, i) => {
                const diffPrevDate = i == 0 || cur.startAt.getDate() != visits[i - 1].startAt.getDate();
                const diffNextDate = visits.length - 1 == i || (visits.length > i + 1 && cur.startAt.getDate() != visits[i + 1].startAt.getDate());
                return <>
                    {diffPrevDate && <div className="w-full">
                        <h4 className="scroll-m-20 text-base tracking-tight">{formatDate(cur.startAt, "E, dd MMM yyyy")}</h4>
                    </div>}
                    <div key={`${cur.name}-${cur.startAt.getTime()}`}
                        className={cn("flex gap-4 cursor-pointer", i > 0 && cur.startAt.getDate() != visits[i - 1].startAt.getDate() && "border-top border-gray-300")}
                        onClick={() => map.setView(cur.coordinates)}>
                        <div className="flex flex-col">
                            {i > 0 && <div className={cn("w-1 h-full mx-auto", !diffPrevDate && "bg-gray-300")} />}
                            <div className="items-center mx-2 my-4">
                                {cur.name === 'Moving' ? <CarFront className="mx-auto" /> : <CircleDot className="mx-auto" />}
                            </div>
                            {<div className={cn("w-1 h-full mx-auto", !diffNextDate && "bg-gray-300")} />}
                        </div>
                        <div className="flex flex-col gap-1 py-4">
                            <span className="font-semibold">{cur.name ?? '?'}</span>
                            {cur.name === 'Moving' && <div>
                                <span>{cur.distance?.toFixed(2)} km · </span>
                                <span>{formatDistanceStrict(cur.endAt, cur.startAt)}</span>
                            </div>}
                            {cur.address && <span>{cur.address}</span>}
                            <span>{cur.startAt.toLocaleTimeString()} - {cur.endAt.toLocaleTimeString()}</span>
                        </div>
                    </div>
                </>
            })}
            {visits.length === 0 && <span>No location recorded</span>}
        </div>
    </MapControl>
  );
}

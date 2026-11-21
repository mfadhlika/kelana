import { cn } from "@/lib/utils";
import { formatDate, formatDistanceStrict } from "date-fns";
import { CarFront, CircleDot, CircleQuestionMark, EllipsisVertical } from "lucide-react";
import { MapControl } from "./map-control";
import { useIsMobile } from "@/hooks/use-mobile";
import type { Visit } from "@/types/visit";
import { useMap } from "react-leaflet";
import {
  Combobox,
  ComboboxContent,
  ComboboxEmpty,
  ComboboxInput,
  ComboboxItem,
  ComboboxList,
} from "@/components/ui/combobox";
import { useEffect, useState, type RefObject } from "react";
import type { PlaceProperties } from "@/types/properties";
import type { Feature, FeatureCollection, Point } from "geojson";
import { placeService } from "@/services/place-service";
import { Button } from "./ui/button";
import { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem, } from "@/components/ui/dropdown-menu";

export type TimelineProps = {
  visits: Visit[];
  featureGroupRefs: RefObject<(L.FeatureGroup | null)[]>;
};

export function PlaceComboBox(visit: Visit) {
  const [places, setPlaces] = useState<FeatureCollection<Point, PlaceProperties>>();

  useEffect(() => {
    const { lng, lat } = visit.coordinates;
    placeService.findPlaces(lng, lat, 100).then(res => setPlaces(res.data));
  }, [visit]);

  return (
    <Combobox
      items={places?.features}
      itemToStringValue={(place: Feature<Point, PlaceProperties>) => place.properties.name}
    >
      <ComboboxInput value={visit.name} placeholder={places?.features.at(0)?.properties?.name} />
      <ComboboxContent>
        <ComboboxEmpty>No items found.</ComboboxEmpty>
        <ComboboxList>
          {(place) => (
            <ComboboxItem key={place.properties.id} value={place}>
              {place.properties.name}
            </ComboboxItem>
          )}
        </ComboboxList>
      </ComboboxContent>
    </Combobox>
  );
}

const ItemIcon = (cur: Visit) => {
  switch (cur.name) {
    case undefined:
      return <CircleQuestionMark />
    case "Moving":
      return <CarFront />
    default:
      return <CircleDot />
  }
}

type ItemDropdownProps = {
  onEdit: () => void,
  onCancelEdit: () => void,
  editMode: boolean,
  open: boolean,
  onOpenChange: (open: boolean) => void
}

const ItemDropdown = ({ onEdit, onCancelEdit, editMode, open, onOpenChange }: ItemDropdownProps) => {
  return (
    <DropdownMenu open={open} onOpenChange={onOpenChange}>
      <DropdownMenuTrigger asChild>
        <Button size="icon" variant="ghost">
          <EllipsisVertical />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start">
        {editMode ? <>
          <DropdownMenuItem onClick={onCancelEdit}>
            Cancel
          </DropdownMenuItem>
        </> : <>
          <DropdownMenuItem onClick={onEdit}>
            Edit
          </DropdownMenuItem>
        </>}
      </DropdownMenuContent>

    </DropdownMenu >
  );
}

type ItemProps = {
  i: number,
  cur: Visit,
  visits: Visit[],
  diffPrevDate: boolean,
  diffNextDate: boolean,
  featureGroufRef: L.FeatureGroup | null,
  openMenuIndex?: number
  setOpenMenuIndex: (i?: number) => void
}

const Item = ({ i, cur, visits, diffPrevDate, diffNextDate, featureGroufRef, openMenuIndex, setOpenMenuIndex }: ItemProps) => {
  const map = useMap();
  const [isEditMode, setEditMode] = useState(false);

  const onMouseEnter = () => {
    featureGroufRef?.bringToFront();
    featureGroufRef?.setStyle({
      color: "red",
    });
  }

  const onMouseLeave = () => {
    featureGroufRef?.bringToBack();
    featureGroufRef?.setStyle({
      color: "#3388ff",
    });
  }

  return (
    <div
      key={`${cur.name}-${cur.startAt.getTime()}`}
      className={cn(
        "flex gap-4 cursor-pointer",
        i > 0 &&
        cur.startAt.getDate() != visits[i - 1].startAt.getDate() &&
        "border-top border-gray-300",
      )}
      onClick={() => map.setView(cur.coordinates)}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
    >
      <div className="flex flex-col">
        {i > 0 && (
          <div
            className={cn(
              "w-1 h-full mx-auto",
              !diffPrevDate && "bg-gray-300",
            )}
          />
        )}
        <div className="items-center mx-2 my-4">
          <ItemIcon {...cur} />
        </div>
        <div
          className={cn(
            "w-1 h-full mx-auto",
            !diffNextDate && "bg-gray-300",
          )}
        />
      </div>
      <div className="flex flex-col gap-1 py-4 w-full">
        {isEditMode ? <PlaceComboBox {...cur} /> : <span>{cur.name ?? "Missing visit"}</span>}
        {cur.name === "Moving" && (
          <div>
            <span>{cur.distance?.toFixed(2)} km · </span>
            <span>
              {formatDistanceStrict(cur.endAt, cur.startAt)}
            </span>
          </div>
        )}
        {cur.address && <span>{cur.address}</span>}
        <span>
          {cur.startAt.toLocaleTimeString()} -{" "}
          {cur.endAt.toLocaleTimeString()}
        </span>
      </div>
      <div className="items-center mx-2 my-4">
        <ItemDropdown
          onEdit={() => setEditMode(true)}
          onCancelEdit={() => setEditMode(false)}
          editMode={isEditMode}
          open={openMenuIndex == i}
          onOpenChange={(open) => setOpenMenuIndex(open ? i : undefined)} />
      </div>
    </div>
  );
}

export function Timeline({ visits, featureGroupRefs }: TimelineProps) {
  const isMobile = useIsMobile();
  const [openMenuIndex, setOpenMenuIndex] = useState<number | undefined>();

  return (
    <MapControl
      position={isMobile ? "bottomright" : "topleft"}
      disableClickPropagation={true}
      disableScrollPropagation={true}
    >
      <div className="leaflet-touch bg-sidebar rounded-xl border border-gray-300 w-[calc(100vw-20px)] md:w-[20vw] max-h-[25vh] md:max-h-[calc(90vh)] overflow-y-auto flex flex-col p-2">
        {visits.map((cur, i) => {
          const diffPrevDate =
            i == 0 || cur.startAt.getDate() != visits[i - 1].startAt.getDate();
          const diffNextDate =
            visits.length - 1 == i ||
            (visits.length > i + 1 &&
              cur.startAt.getDate() != visits[i + 1].startAt.getDate());
          return (
            <>
              {diffPrevDate && (
                <div className="w-full" >
                  <h4 className="scroll-m-20 text-base tracking-tight">
                    {formatDate(cur.startAt, "E, dd MMM yyyy")}
                  </h4>
                </div>
              )}
              <Item
                i={i}
                cur={cur}
                visits={visits}
                diffNextDate={diffNextDate}
                diffPrevDate={diffPrevDate}
                featureGroufRef={featureGroupRefs.current[i]}
                openMenuIndex={openMenuIndex}
                setOpenMenuIndex={setOpenMenuIndex} />
            </>
          );
        })}
        {visits.length === 0 && <span>No location recorded</span>}
      </div>
    </MapControl>
  );
}

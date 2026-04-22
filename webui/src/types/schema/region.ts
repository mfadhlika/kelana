import z from "zod";

export const regionFormSchema = z.object({
    desc: z.string(),
    rid: z.optional(z.string()),
    beaconUUID: z.optional(z.uuid("v4")),
    beaconMajor: z.optional(z.number()),
    beaconMinor: z.optional(z.number()),
});

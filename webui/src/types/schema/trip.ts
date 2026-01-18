import { isAfter } from "date-fns";
import z from "zod";

export const tripFormSchema = z.object({
    title: z.string(),
    startAt: z.date(),
    endAt: z.date(),
    uuid: z.optional(z.uuid()),
    isPublic: z.boolean()
}).refine((data) => isAfter(data.endAt, data.startAt), {
    path: ['endAt'],
    error: 'End at must be after start at'
});

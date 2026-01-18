import { isAfter } from "date-fns";
import z from "zod";

export const exportFormSchema = z.object({
    startAt: z.date(),
    endAt: z.date()
}).refine((data) => isAfter(data.endAt, data.startAt), {
    path: ['endAt'],
    error: 'End at must be after start at'
});

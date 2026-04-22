import type z from "zod";
import type { regionFormSchema } from "../schema/region";

export type Region = z.infer<typeof regionFormSchema>;;

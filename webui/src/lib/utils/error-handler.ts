import { AuthenticationError } from "@/types/errors";
import { toast } from "sonner";

export const handleError = (err: unknown, message?: string) => {
    if (err instanceof AuthenticationError) return;
    if (!message) message = `${err}`;
    else message = `${message}: ${err}`;
    toast.error(message);
}

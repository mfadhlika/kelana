import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
    DialogTrigger
} from "@/components/ui/dialog.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Loader2Icon, Upload } from "lucide-react";
import { Input } from "@/components/ui/input.tsx";
import { zodResolver } from "@hookform/resolvers/zod"
import { Controller, useForm } from "react-hook-form";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group.tsx";
import { useState } from "react";
import { cn } from "@/lib/utils";
import { toast } from "sonner";
import type { Import as ImportRequest } from "@/types/requests/import";
import { importFormSchema } from "@/types/schema/import";
import { importService } from "@/services/import-service";
import { Field, FieldContent, FieldLabel, FieldLegend, FieldSet } from "./ui/field";



export const ImportDialog = ({ className }: React.ComponentProps<"div">) => {
    const [open, setOpen] = useState(false);

    const form = useForm<ImportRequest>({
        resolver: zodResolver(importFormSchema),
        defaultValues: {
            source: "dawarich",
        }
    });

    const { formState } = form;

    const fileRef = form.register("file");

    const onSubmit = (values: ImportRequest) => {
        importService.createImport(values)
            .then(_ => {
                toast.success("File uploaded succesfully");
                setOpen(false);
            })
            .catch(err => {
                toast.error(`Failed to get user's devices: ${err}`);
            });
    }

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button variant="outline" className={cn("shadow-xs", className)}>
                    <Upload />
                    Import
                </Button>
            </DialogTrigger>
            <DialogContent className="z-10000">
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
                    <DialogHeader>
                        <DialogTitle>Import locations</DialogTitle>
                        <DialogDescription>
                            Upload exported location from another sources
                        </DialogDescription>
                    </DialogHeader>
                    <Controller control={form.control} name="source" render={({ field }) => (
                        <FieldSet>
                            <FieldLegend>Source</FieldLegend>
                            <RadioGroup
                                name={field.name}
                                onValueChange={field.onChange}
                                defaultValue={field.value}>
                                <FieldLabel htmlFor="form-import-source-dawarich">
                                    <Field orientation="horizontal">
                                        <FieldContent>Dawarich</FieldContent>
                                        <RadioGroupItem id="form-import-source-dawarich" value="dawarich" />
                                    </Field>
                                </FieldLabel>
                            </RadioGroup>
                        </FieldSet>
                    )} />
                    <Controller control={form.control} name="file"
                        render={() => (
                            <Field>
                                <FieldLabel>File to import</FieldLabel>
                                <Input type="file" {...fileRef} />
                            </Field>
                        )} />
                    <Button type="submit" disabled={formState.isSubmitting}>
                        {formState.isSubmitting && <Loader2Icon className="animate-spin" />}
                        Import
                    </Button>
                </form>
            </DialogContent>
        </Dialog>
    );
};

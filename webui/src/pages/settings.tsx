import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { axiosInstance } from "@/lib/request";
import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import z from "zod";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { useEffect, useState } from "react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from "@/components/ui/card";
import { useNavigate } from "react-router";
import { Header } from "@/components/header";
import { toast } from "sonner";
import type { Integration } from "@/types/integration";
import { useAuthStore } from "@/hooks/use-auth";
import { backupService } from "@/services/backup-service";
import { integrationService } from "@/services/integration-service";
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";

const accountFormSchema = z.object({
    username: z.string(),
    password: z.string(),
    confirmPassword: z.string(),
}).refine((data) => data.password === data.confirmPassword, {
    path: ['confirmPassword'],
    message: 'Password does not match'
});

const owntracksFormSchema = z.object({
    username: z.string(),
    password: z.string(),
});

const overlandFormSchema = z.object({
    apiKey: z.string(),
});


function AccountSettingsTab() {
    const { userInfo, logout } = useAuthStore();
    const navigate = useNavigate();

    const accountForm = useForm<z.infer<typeof accountFormSchema>>({
        resolver: zodResolver(accountFormSchema),
        defaultValues: {
            username: userInfo?.username ?? "",
            password: "",
            confirmPassword: "",
        }
    });

    const onSubmit = (values: z.infer<typeof accountFormSchema>) => {
        axiosInstance.put("v1/user", {
            username: values.username,
            password: values.password,
        })
            .then((_res) => {
                if (values.username !== userInfo?.username) {
                    logout();
                    navigate("/login");
                }
            })
            .catch(err => {
                toast.error(`Failed to updated user's account: ${err}`);
            });
    }

    return (
        <form onSubmit={accountForm.handleSubmit(onSubmit)} className="flex flex-col gap-4">
            <Card>
                <CardHeader>
                    <CardTitle>Account</CardTitle>
                    <CardDescription>
                        Change your username and password here. After saving, you&apos;ll be logged
                        out.
                    </CardDescription>
                </CardHeader>
                <CardContent className="grid gap-6">
                    <Controller control={accountForm.control} name="username" render={({ field }) => (
                        <Field>
                            <FieldLabel>Username</FieldLabel>
                            <Input type="text" autoComplete="username"  {...field} />
                        </Field>
                    )} />
                    <Controller control={accountForm.control} name="password" render={({ field }) => (
                        <Field>
                            <FieldLabel>Password</FieldLabel>
                            <Input type="password" autoComplete="password"  {...field} />
                        </Field>
                    )} />
                    <Controller control={accountForm.control} name="confirmPassword" render={({ field, fieldState }) => (
                        <Field>
                            <FieldLabel>Confirm Password</FieldLabel>
                            <Input type="password" autoComplete="password" {...field} />
                            {fieldState.error && <FieldError>{fieldState.error.message}</FieldError>}
                        </Field>
                    )} />
                </CardContent>
                <CardFooter>
                    <Button type="submit" className="w-[100px] self-end" disabled={accountForm.formState.isSubmitting}>Submit</Button>
                </CardFooter>

            </Card>
        </form>

    );
}

function OwntracksIntegrationItem({ integration, doSubmit }: { integration: Integration, doSubmit: (integration: Integration) => void }) {
    const owntracksForm = useForm<z.infer<typeof owntracksFormSchema>>({
        resolver: zodResolver(owntracksFormSchema),
        defaultValues: {
            username: integration?.owntracksUsername ?? "",
        }
    });

    const onSubmit = (values: z.infer<typeof owntracksFormSchema>) => {
        doSubmit({
            ...integration,
            owntracksUsername: values.username,
            owntracksPassword: values.password,
        });
    }

    return (
        <>
            <form onSubmit={owntracksForm.handleSubmit(onSubmit)} className="flex flex-col gap-4">
                <FieldGroup>
                    <Controller control={owntracksForm.control} name="username" render={({ field }) => (
                        <Field>
                            <FieldLabel htmlFor="form-owntracks-username">Username</FieldLabel>
                            <Input id="form-owntracks-username" type="text" autoComplete="username"  {...field} />
                        </Field>
                    )} />
                    <Controller control={owntracksForm.control} name="password" render={({ field }) => (
                        <Field>
                            <FieldLabel htmlFor="form-owntracks-password">Password</FieldLabel>
                            <Input id="form-owntracks-password" type="password" autoComplete="password"  {...field} />
                        </Field>
                    )} />
                    <Button type="submit" className="w-[100px] self-end" disabled={owntracksForm.formState.isSubmitting}>Save</Button>
                </FieldGroup>
            </form>
            <div style={{ "width": "100%", "display": "flex", "justifyContent": "space-between", "marginTop": "2.5rem" }}>
                <span>Publish waypoints</span>
                <Button onClick={() => integrationService.sendOwntracksCommand("setWaypoints")}>Publish waypoints</Button>
            </div>
        </>
    );
}

function OverlandIntegrationItem({ integration, doSubmit }: { integration: Integration, doSubmit: (integration: Integration) => void }) {
    const overlandForm = useForm<z.infer<typeof overlandFormSchema>>({
        resolver: zodResolver(overlandFormSchema),
        defaultValues: {
            apiKey: integration?.overlandApiKey ?? "",
        }
    });

    const onSubmit = (_: z.infer<typeof overlandFormSchema>) => {
        doSubmit({
            ...integration,
            overlandApiKey: "",
        });
    }

    return (
        <form onSubmit={overlandForm.handleSubmit(onSubmit)} className="flex flex-col gap-4">
            <Controller control={overlandForm.control} name="apiKey" render={({ field }) => (
                <Field>
                    <FieldLabel>Api Key</FieldLabel>
                    <Input type="text" disabled {...field} />
                </Field>
            )} />
            <Button type="submit" className="w-[100px] self-end" disabled={overlandForm.formState.isSubmitting}>Reset</Button>
        </form>
    );
}

function IntegrationSettingsTab() {
    const [integration, setIntegration] = useState<Integration>({
        owntracksUsername: "",
        owntracksPassword: "",
        overlandApiKey: ""
    });

    useEffect(() => {
        integrationService.fetchIntegration().then(res => {
            setIntegration({ ...res.data });
        }).catch(err => {
            console.error(err);
            toast.error("failed fetch integration settings", err);
        });
    }, []);

    const doSubmit = (data: Integration) => {
        integrationService.submitIntegration(data)
            .then(res => {
                setIntegration({ ...res.data });
                toast.success("Integration saved succesfully");
            })
            .catch(err => {
                toast.error(`Failed to update integration: ${err}`);
            });
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle>Integration</CardTitle>
                <CardDescription>
                    Configure integration with other apps here.
                </CardDescription>
            </CardHeader>
            <CardContent className="grid gap-6">
                <Accordion key={JSON.stringify(integration)} type="multiple">
                    <AccordionItem value="owntracks">
                        <AccordionTrigger>Owntracks</AccordionTrigger>
                        <AccordionContent>
                            <OwntracksIntegrationItem integration={integration} doSubmit={doSubmit} />
                        </AccordionContent>
                    </AccordionItem>
                    <AccordionItem value="overland">
                        <AccordionTrigger>Overland</AccordionTrigger>
                        <AccordionContent>
                            <OverlandIntegrationItem integration={integration} doSubmit={doSubmit} />
                        </AccordionContent>
                    </AccordionItem>
                </Accordion>
            </CardContent>
        </Card>
    );
}

function BackupSettingsTab() {
    const createBackup = () => {
        backupService.createBackup().then(() => {
            toast.info("Backup successfully created");
        }).catch(() => {
            toast.error("Failed to create backup");
        })
    };

    return (
        <Card>
            <CardHeader>
                <CardTitle>System</CardTitle>
                <CardDescription>
                    Configure system here.
                </CardDescription>
            </CardHeader>
            <CardContent className="grid gap-6">
                <Accordion type="multiple">
                    <AccordionItem value="backup">
                        <AccordionTrigger>Backup</AccordionTrigger>
                        <AccordionContent>
                            <div style={{ "width": "100%", "display": "flex", "justifyContent": "space-between" }}>
                                <span>Create backup</span>
                                <Button onClick={createBackup}>Backup</Button>
                            </div>
                        </AccordionContent>
                    </AccordionItem>
                </Accordion>
            </CardContent>
        </Card>
    );
}

export default function SettingsPage() {
    return (
        <Tabs defaultValue="account" className="w-full">
            <Header>
                <TabsList>
                    <TabsTrigger value="account">Account</TabsTrigger>
                    <TabsTrigger value="integration">Integration</TabsTrigger>
                    <TabsTrigger value="system">System</TabsTrigger>
                </TabsList>
            </Header>
            <div className="pr-4 pl-4">
                <TabsContent value="account">
                    <AccountSettingsTab />
                </TabsContent>
                <TabsContent value="integration">
                    <IntegrationSettingsTab />
                </TabsContent>
                <TabsContent value="system">
                    <BackupSettingsTab />
                </TabsContent>
            </div>
        </Tabs>
    );
}

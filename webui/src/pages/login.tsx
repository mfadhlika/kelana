import { useEffect } from "react";
import { useNavigate } from "react-router";
import { Input } from "@/components/ui/input.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Card, CardContent } from "@/components/ui/card";
import type { Response } from "@/types/response";
import { Controller, useForm } from "react-hook-form";
import type { Login as LoginRequest } from "@/types/requests/login";
import { zodResolver } from "@hookform/resolvers/zod";
import type { AxiosError } from "axios";
import { loginFormSchema } from "@/types/schema/login";
import { authService } from "@/services/auth-service";
import { useAuthStore } from "@/hooks/use-auth";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { toast } from "sonner";

export default function LoginPage() {
    const { userInfo, login } = useAuthStore();
    const navigate = useNavigate();

    const loginForm = useForm<LoginRequest>({
        resolver: zodResolver(loginFormSchema),
        defaultValues: {}
    });

    const onSubmit = (values: LoginRequest) => {
        authService.login(values).then(({ data }) => {
            login(data.accessToken);
            navigate("/");
        })
            .catch((err: AxiosError<Response>) => {
                toast.error(err.response?.data?.message);
            });
    }

    useEffect(() => {
        if (userInfo) navigate("/");
    }, [navigate, userInfo])

    return (
        <div className="bg-muted flex min-h-svh flex-col items-center justify-center gap-6 p-6 md:p-10">
            <div className="flex w-full max-w-sm flex-col gap-6">
                <a href="#" className="flex items-center gap-2 self-center font-medium">
                    <img src="/kelana.svg" className="w-6 h-6" />
                    Kelana
                </a>
                <div className="flex flex-col gap-6">
                    <Card className="w-full max-w-sm">
                        <CardContent>
                            <form onSubmit={loginForm.handleSubmit(onSubmit)} className="flex flex-col gap-4">
                                <FieldGroup>
                                    <Controller control={loginForm.control} name="username" render={({ field }) => (
                                        <Field>
                                            <FieldLabel htmlFor={`form-login-${field.name}`}>Username</FieldLabel>
                                            <Input id={`form-login-${field.name}`} type="text" autoComplete="username"  {...field} />
                                        </Field>
                                    )} />
                                    <Controller control={loginForm.control} name="password" render={({ field }) => (
                                        <Field>
                                            <FieldLabel htmlFor={`form-login-${field.name}`}>Password</FieldLabel>
                                            <Input id={`form-login-${field.name}`} type="password" autoComplete="password"  {...field} />
                                        </Field>
                                    )} />
                                    <Button type="submit" className="w-full">Login</Button>
                                </FieldGroup>
                            </form>
                        </CardContent>
                    </Card>
                </div>
            </div>
        </div >
    );
}

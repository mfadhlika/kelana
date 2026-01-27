import { expect, vi, describe, it, beforeEach } from 'vitest';
import { render, type RenderResult } from 'vitest-browser-react'
import LoginPage from './login';
import { BrowserRouter } from 'react-router';
import { Toaster } from 'sonner';

describe('login page', async () => {
    let loginPage: RenderResult;

    beforeEach(() => {
        loginPage = render(
            <BrowserRouter>
                <LoginPage />
                <Toaster />
            </BrowserRouter>
        );
    });

    it('should render login page', async () => {
        const header = loginPage.getByText('Kelana');

        await expect.element(header).toHaveTextContent('Kelana');
    });

    describe('login', async () => {
        const mocks = vi.hoisted(() => ({
            login: vi.fn(),
            navigate: vi.fn()
        }))

        vi.mock('@/services/auth-service', async () => {
            return {
                authService: {
                    login: mocks.login
                }
            };
        });

        vi.mock('react-router', async (importActual) => {
            const actual = await importActual<typeof import('react-router')>();
            return {
                ...actual,
                useNavigate: () => mocks.navigate
            }
        });

        it('should navigate if login success', async () => {
            mocks.login.mockResolvedValue({
                data: {
                    accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30'
                },
                message: 'success'
            });

            await loginPage.getByLabelText("Username").fill("test");
            await loginPage.getByLabelText("Password").fill("test");
            await loginPage.getByText("Login").click();

            expect(mocks.login).toHaveBeenCalledWith({
                username: 'test',
                password: 'test'
            });

            expect(mocks.navigate).toHaveBeenCalledWith("/");
        });

        it('should show error message if login failed', async () => {
            mocks.login.mockRejectedValue({
                response: {
                    data: {
                        message: 'invalid username/password'
                    }
                }
            });

            await loginPage.getByLabelText("Username").fill("test");
            await loginPage.getByLabelText("Password").fill("nottest");
            await loginPage.getByText("Login").click();

            expect(mocks.login).toHaveBeenCalledWith({
                username: 'test',
                password: 'test'
            });

            expect(mocks.navigate).toHaveBeenCalledWith("/");

            await expect.element(loginPage.getByText("invalid username/password")).toBeVisible();
        });
    });
});



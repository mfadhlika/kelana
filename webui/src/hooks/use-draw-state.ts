import { create } from "zustand"

export type DrawState = "view" | "draw:circle:start" | "draw:circle:resize" | "draw:square:start" | "draw:square:resize" | "draw:edit" | "draw:end"

export type DrawStore = {
    state: DrawState,
    drawCircle: () => void,
}

export const useDrawState = create<DrawState>(() => ("view"))

export const setDrawState = (state: DrawState) => useDrawState.setState(state);

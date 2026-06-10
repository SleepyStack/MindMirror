import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";

const TOKEN_STORAGE_KEY = "mm_token";

type AuthContextValue = {
	isAuthenticated: boolean;
	loginUser: (token: string) => void;
	logoutUser: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

type AuthProviderProps = {
	children: ReactNode;
};

export function AuthProvider({ children }: AuthProviderProps) {
	const [isAuthenticated, setIsAuthenticated] = useState(() => Boolean(window.localStorage.getItem(TOKEN_STORAGE_KEY)));

	useEffect(() => {
		const token = window.localStorage.getItem(TOKEN_STORAGE_KEY);
		setIsAuthenticated(Boolean(token));
	}, []);

	const loginUser = (token: string) => {
		window.localStorage.setItem(TOKEN_STORAGE_KEY, token);
		setIsAuthenticated(true);
	};

	const logoutUser = () => {
		window.localStorage.removeItem(TOKEN_STORAGE_KEY);
		setIsAuthenticated(false);
		window.location.reload();
	};

	const value = useMemo(
		() => ({
			isAuthenticated,
			loginUser,
			logoutUser,
		}),
		[isAuthenticated]
	);

	return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
	const context = useContext(AuthContext);

	if (!context) {
		throw new Error("useAuth must be used within an AuthProvider");
	}

	return context;
}

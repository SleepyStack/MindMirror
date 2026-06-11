const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const TOKEN_STORAGE_KEY = "mm_token";

export type LoginPayload = {
	email: string;
	password: string;
};

export type RegisterPayload = {
	username: string;
	email: string;
	password: string;
};

export type AuthResponsePayload = {
	token: string;
};

type RequestOptions = RequestInit & {
	headers?: HeadersInit;
};

function getToken(): string | null {
	if (typeof window === "undefined") {
		return null;
	}

	return window.localStorage.getItem(TOKEN_STORAGE_KEY);
}

function buildUrl(endpoint: string): string {
	if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
		return endpoint;
	}

	return `${API_BASE_URL}${endpoint}`;
}

async function parseResponse<T>(response: Response): Promise<T> {
	if (!response.ok) {
		const errorText = await response.text();
		throw new Error(errorText || `Request failed with status ${response.status}`);
	}

	return (await response.json()) as T;
}

export async function fetchWithAuth<T>(endpoint: string, options: RequestOptions = {}): Promise<T> {
	const token = getToken();
	const headers = new Headers(options.headers);

	if (token) {
		headers.set("Authorization", `Bearer ${token}`);
	}

	if (options.body && !headers.has("Content-Type")) {
		headers.set("Content-Type", "application/json");
	}

	const response = await fetch(buildUrl(endpoint), {
		...options,
		headers,
	});

	return parseResponse<T>(response);
}

async function postJson<TResponse, TBody>(endpoint: string, body: TBody): Promise<TResponse> {
	const response = await fetch(buildUrl(endpoint), {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
		},
		body: JSON.stringify(body),
	});

	return parseResponse<TResponse>(response);
}

export function login(email: string, password: string): Promise<AuthResponsePayload> {
	return postJson<AuthResponsePayload, LoginPayload>("/api/auth/login", {
		email,
		password,
	});
}

export function register(payload: RegisterPayload): Promise<AuthResponsePayload> {
	return postJson<AuthResponsePayload, RegisterPayload>("/api/auth/register", payload);
}

export type SessionHistoryResponse = {
    id: number;
    agentId: string;
    conversationId: string;
    status: string;
    mainTopic: string;
    summaryText: string;
    emotionStart: string;
    emotionEnd: string;
    actionStep: string;
    createdAt: string;
    username: string;
    email: string;
};

export type StartSessionResponse = {
    url: string;
};

export function getSessionHistory(): Promise<SessionHistoryResponse[]> {
    return fetchWithAuth<SessionHistoryResponse[]>("/api/session/history");
}

export function startSession(lang: string = "EN"): Promise<StartSessionResponse> {
    return fetchWithAuth<StartSessionResponse>(`/api/session/start/${lang}`, {
        method: "POST"
    });
}

export type TranscriptResponseDto = {
    conversationId: string;
    syncStatus: string;
    payload: any; 
};

export function getTranscript(conversationId: string): Promise<TranscriptResponseDto> {
    return fetchWithAuth<TranscriptResponseDto>(`/api/transcripts/${conversationId}`);
}
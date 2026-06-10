import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { register } from "../utils/api";
import { Sparkles, UserPlus } from "lucide-react";

export default function Register() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const { loginUser } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      const response = await register({ username, email, password });
      if (response && response.token) {
        // Auto-login the user immediately after a successful registration
        loginUser(response.token);
        navigate("/dashboard");
      } else {
        setError("Account created but no credentials token was returned.");
      }
    } catch (err: any) {
      setError(err.message || "Registration failed. Account might already exist.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-twilight-bg flex items-center justify-center p-4 relative overflow-hidden">
      {/* Cloud-like ambient background shapes */}
      <div className="absolute top-[-10%] right-[-10%] w-72 h-72 bg-twilight-secondary rounded-full mix-blend-multiply filter blur-3xl opacity-40 animate-pulse"></div>
      <div className="absolute bottom-[-10%] left-[-10%] w-80 h-80 bg-twilight-primary rounded-full mix-blend-multiply filter blur-3xl opacity-40 animate-pulse delay-700"></div>

      <div className="bg-twilight-card rounded-3xl p-8 md:p-10 shadow-[0_20px_50px_rgba(189,178,255,0.18)] border border-purple-100 max-w-md w-full relative z-10">
        
        {/* Header Block */}
        <div className="text-center mb-6">
          <div className="inline-flex items-center justify-center p-3 bg-twilight-bg text-twilight-primary rounded-2xl mb-3">
            <UserPlus className="w-8 h-8 stroke-twilight-primary" />
          </div>
          <h2 className="text-3xl font-bold text-twilight-text tracking-tight">Join MindMirror</h2>
          <p className="text-sm text-twilight-text/70 mt-1">Create an account to start your emotional reflection space</p>
        </div>

        {error && (
          <div className="mb-5 p-4 bg-red-50 text-red-600 rounded-2xl text-sm border border-red-100">
            ⚠️ {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold text-twilight-text mb-1.5 pl-1">Your Name</label>
            <input
              type="text"
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Chirag"
              className="w-full px-5 py-3 bg-twilight-bg/60 border border-purple-100/80 rounded-2xl text-twilight-text placeholder-twilight-text/40 focus:outline-none focus:ring-2 focus:ring-twilight-primary/50 focus:bg-white transition-all"
            />
          </div>

          <div>
            <label className="block text-sm font-semibold text-twilight-text mb-1.5 pl-1">Email Address</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="name@domain.com"
              className="w-full px-5 py-3 bg-twilight-bg/60 border border-purple-100/80 rounded-2xl text-twilight-text placeholder-twilight-text/40 focus:outline-none focus:ring-2 focus:ring-twilight-primary/50 focus:bg-white transition-all"
            />
          </div>

          <div>
            <label className="block text-sm font-semibold text-twilight-text mb-1.5 pl-1">Password</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Create a password"
              className="w-full px-5 py-3 bg-twilight-bg/60 border border-purple-100/80 rounded-2xl text-twilight-text placeholder-twilight-text/40 focus:outline-none focus:ring-2 focus:ring-twilight-primary/50 focus:bg-white transition-all"
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full mt-4 bg-twilight-primary text-white font-bold py-3.5 px-6 rounded-2xl shadow-md shadow-purple-200 hover:opacity-95 transition-all active:scale-[0.99] disabled:opacity-50 flex items-center justify-center gap-2 text-lg"
          >
            {isLoading ? (
              <span className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
            ) : (
              <>
                Create Account <Sparkles className="w-5 h-5" />
              </>
            )}
          </button>
        </form>

        <div className="mt-6 text-center text-sm text-twilight-text/70">
          Already have an account?{" "}
          <Link to="/login" className="text-twilight-primary font-semibold hover:underline">
            Login here
          </Link>
        </div>
      </div>
    </div>
  );
}
import React, { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { X } from "lucide-react";

export default function SessionRoom() {
  const navigate = useNavigate();
  const location = useLocation();
  const [iframeUrl, setIframeUrl] = useState<string | null>(null);

  useEffect(() => {
    // Catch the URL passed from the Dashboard
    const urlFromState = location.state?.iframeUrl;
    if (!urlFromState) {
      navigate("/dashboard"); // Kick them back if they try to access this directly without a URL
    } else {
      setIframeUrl(urlFromState);
    }
  }, [location, navigate]);

  if (!iframeUrl) return null;

  return (
    <div className="w-screen h-screen bg-[#111] relative overflow-hidden flex flex-col">
      
      {/* Floating Control Bar overlay */}
      <div className="absolute top-0 left-0 w-full p-4 flex justify-between items-center z-50 bg-gradient-to-b from-black/60 to-transparent">
        <div className="flex items-center gap-3 px-4 py-2 bg-black/40 backdrop-blur-md rounded-full border border-white/10">
          <span className="relative flex h-3 w-3">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-500 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-3 w-3 bg-red-500"></span>
          </span>
          <span className="font-semibold text-white text-sm tracking-wide">Live Session Active</span>
        </div>
        
        <button 
          onClick={() => navigate("/dashboard")}
          className="flex items-center gap-2 px-5 py-2.5 bg-red-500/20 hover:bg-red-500 text-red-500 hover:text-white backdrop-blur-md rounded-full border border-red-500/30 transition-all font-semibold text-sm"
        >
          <X className="w-4 h-4" /> End & Return
        </button>
      </div>

      {/* The EXACT Trugen Implementation */}
      <iframe 
        src={iframeUrl}
        allow="microphone; camera; fullscreen; display-capture"
        style={{ width: "100%", height: "100vh", border: "none" }}
        className="w-full h-full object-cover"
        title="Trugen AI Companion"
      />
    </div>
  );
}
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { getSessionHistory, startSession, getTranscript, type SessionHistoryResponse } from "../utils/api";
import { LayoutDashboard, LogOut, Sparkles, ArrowRight, Calendar, FileText, X } from "lucide-react";
import jsPDF from "jspdf";

export default function Dashboard() {
  const { logoutUser } = useAuth();
  const navigate = useNavigate();
  const [sessions, setSessions] = useState<SessionHistoryResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isStartingSession, setIsStartingSession] = useState(false);
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  // Modal State for viewing session summaries
  const [selectedSession, setSelectedSession] = useState<SessionHistoryResponse | null>(null);

  useEffect(() => {
    loadHistory();
  }, []);

  const loadHistory = async () => {
    try {
      const data = await getSessionHistory();
      setSessions(data);
    } catch (error) {
      console.error("Failed to load history", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleStartSession = async () => {
    setIsStartingSession(true);
    try {
      const response = await startSession();
      
      const userName = sessions.length > 0 && sessions[0].username ? sessions[0].username : "Chirag";
      const userEmail = sessions.length > 0 && sessions[0].email ? sessions[0].email : "user@mindmirror.com";
      
      const bypassUrl = `${response.url}?username=${encodeURIComponent(userName)}&id=${encodeURIComponent(userEmail)}&context=${encodeURIComponent("MindMirror Session")}`;
      
      navigate("/session", { state: { iframeUrl: bypassUrl } });
      
    } catch (error) {
      console.error("Failed to start session", error);
    } finally {
      setIsStartingSession(false);
    }
  };

  const downloadTranscriptPDF = async (session: SessionHistoryResponse) => {
    setDownloadingId(session.id);
    try {
      const transcriptData = await getTranscript(session.conversationId);
      
      const doc = new jsPDF();
      const margin = 20;
      let cursorY = margin;

      doc.setFont("helvetica", "bold");
      doc.setFontSize(22);
      doc.setTextColor(189, 178, 255);
      doc.text("MindMirror Reflection", margin, cursorY);
      
      cursorY += 10;
      doc.setFontSize(10);
      doc.setTextColor(100, 100, 100);
      doc.text(`Date: ${new Date(session.createdAt).toLocaleString()}`, margin, cursorY);
      cursorY += 6;
      doc.text(`Topic: ${session.mainTopic || "General Check-in"}`, margin, cursorY);
      cursorY += 15;

      doc.setFontSize(14);
      doc.setTextColor(74, 78, 105);
      doc.text("Session Summary:", margin, cursorY);
      cursorY += 8;
      
      doc.setFont("helvetica", "normal");
      doc.setFontSize(11);
      const splitSummary = doc.splitTextToSize(session.summaryText || "No summary available.", 170);
      doc.text(splitSummary, margin, cursorY);
      cursorY += (splitSummary.length * 6) + 10;

      if (transcriptData.payload) {
        doc.setFont("helvetica", "bold");
        doc.setFontSize(14);
        doc.text("Full Transcript:", margin, cursorY);
        cursorY += 8;

        doc.setFont("helvetica", "normal");
        doc.setFontSize(10);
        
        const rawTranscript = JSON.stringify(transcriptData.payload, null, 2);
        const splitTranscript = doc.splitTextToSize(rawTranscript, 170);
        
        for (let i = 0; i < splitTranscript.length; i++) {
          if (cursorY > 280) {
            doc.addPage();
            cursorY = 20;
          }
          doc.text(splitTranscript[i], margin, cursorY);
          cursorY += 5;
        }
      }

      doc.save(`MindMirror_Transcript_${session.id}.pdf`);

    } catch (error) {
      console.error("Failed to generate PDF", error);
      alert("Failed to download transcript. It may still be processing.");
    } finally {
      setDownloadingId(null);
    }
  };

  return (
    <div className="min-h-screen bg-twilight-bg flex">
      
      {/* SIDEBAR */}
      <aside className="w-64 bg-twilight-card border-r border-purple-100 flex flex-col p-6 shadow-[4px_0_24px_rgba(189,178,255,0.05)] z-10">
        <div className="flex items-center gap-3 mb-10">
          <div className="w-10 h-10 bg-twilight-bg rounded-xl flex items-center justify-center">
            <Sparkles className="w-6 h-6 text-twilight-primary" />
          </div>
          <h1 className="text-xl font-bold text-twilight-text tracking-tight">MindMirror</h1>
        </div>

        <nav className="flex-1 space-y-2">
          <button className="flex items-center gap-3 w-full px-4 py-3 bg-twilight-primary/10 text-twilight-primary rounded-2xl font-semibold transition-all">
            <LayoutDashboard className="w-5 h-5" />
            Dashboard
          </button>
        </nav>

        <button 
          onClick={logoutUser}
          className="flex items-center gap-3 px-4 py-3 text-twilight-text/60 hover:text-red-400 hover:bg-red-50 rounded-2xl transition-all font-semibold"
        >
          <LogOut className="w-5 h-5" />
          Disconnect
        </button>
      </aside>

      {/* MAIN STAGE */}
      <main className="flex-1 p-8 md:p-12 overflow-y-auto relative">
        <header className="flex justify-between items-end mb-10">
          <div>
            <h2 className="text-3xl font-bold text-twilight-text">Your Reflections</h2>
            <p className="text-twilight-text/70 mt-1">Review your past emotional check-ins.</p>
          </div>
          
          <button 
            onClick={handleStartSession}
            disabled={isStartingSession}
            className="bg-twilight-primary text-white font-bold py-3.5 px-6 rounded-2xl shadow-md shadow-purple-200 hover:opacity-95 transition-all active:scale-[0.98] disabled:opacity-50 flex items-center gap-2"
          >
            {isStartingSession ? "Waking Avatar..." : "Start New Session"} 
            <Sparkles className="w-5 h-5" />
          </button>
        </header>

        {/* SESSION GRID */}
        {isLoading ? (
          <div className="animate-pulse flex space-x-4">
            <div className="h-32 bg-twilight-primary/20 rounded-3xl w-full"></div>
          </div>
        ) : sessions.length === 0 ? (
          <div className="bg-twilight-card rounded-3xl p-12 text-center shadow-sm border border-purple-50">
            <div className="w-16 h-16 bg-twilight-bg rounded-full flex items-center justify-center mx-auto mb-4">
              <Calendar className="w-8 h-8 text-twilight-primary" />
            </div>
            <h3 className="text-xl font-bold text-twilight-text mb-2">No history yet</h3>
            <p className="text-twilight-text/60">Start a new session to begin your journey.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {sessions.map((session) => (
              <div key={session.id} className="bg-twilight-card rounded-3xl p-6 shadow-[0_8px_30px_rgba(189,178,255,0.08)] border border-purple-50 hover:border-twilight-primary/30 transition-all flex flex-col h-full">
                
                <div className="flex justify-between items-start mb-4">
                  <span className="text-xs font-bold px-3 py-1 bg-twilight-bg text-twilight-text/70 rounded-full">
                    {new Date(session.createdAt).toLocaleDateString()}
                  </span>
                  <span className={`text-xs font-bold px-3 py-1 rounded-full ${session.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-twilight-secondary/40 text-twilight-text'}`}>
                    {session.status}
                  </span>
                </div>

                <h3 className="text-lg font-bold text-twilight-text mb-2 line-clamp-1">{session.mainTopic || "General Check-in"}</h3>
                <p className="text-sm text-twilight-text/70 flex-1 mb-4 line-clamp-3">
                  {session.summaryText || "Waiting for webhook payload..."}
                </p>

                {session.emotionStart && session.emotionEnd && (
                  <div className="flex items-center gap-2 text-sm font-semibold text-twilight-text/80 bg-twilight-bg/50 p-3 rounded-2xl mb-4">
                    <span>{session.emotionStart}</span>
                    <ArrowRight className="w-4 h-4 text-twilight-primary" />
                    <span>{session.emotionEnd}</span>
                  </div>
                )}

                <div className="flex gap-2 mt-auto">
                  <button 
                    onClick={() => setSelectedSession(session)}
                    className="flex-1 flex items-center justify-center gap-2 py-2.5 bg-twilight-primary/10 text-twilight-primary font-bold rounded-xl hover:bg-twilight-primary/20 transition-colors text-sm"
                  >
                    <FileText className="w-4 h-4" /> View Data
                  </button>
                  <button 
                    onClick={() => downloadTranscriptPDF(session)}
                    disabled={downloadingId === session.id || session.status === 'PENDING'}
                    className="flex-1 flex items-center justify-center gap-2 py-2.5 bg-twilight-bg text-twilight-text font-bold rounded-xl hover:bg-purple-100 transition-colors text-sm disabled:opacity-50"
                  >
                    {downloadingId === session.id ? "..." : "PDF"}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* --- SESSION DETAILS MODAL --- */}
      {selectedSession && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-8">
          <div className="absolute inset-0 bg-twilight-text/20 backdrop-blur-sm transition-opacity" onClick={() => setSelectedSession(null)}></div>
          
          <div className="relative w-full max-w-2xl bg-white rounded-[2rem] shadow-[0_20px_60px_rgba(0,0,0,0.15)] flex flex-col overflow-hidden animate-in fade-in zoom-in-95 duration-200 max-h-[80vh]">
            
            <div className="px-8 py-6 border-b border-purple-50 flex justify-between items-center bg-twilight-bg/30">
              <div>
                <h3 className="text-xl font-bold text-twilight-text">{selectedSession.mainTopic || "Session Details"}</h3>
                <p className="text-sm text-twilight-text/60 mt-1">{new Date(selectedSession.createdAt).toLocaleString()}</p>
              </div>
              <button 
                onClick={() => setSelectedSession(null)}
                className="p-2 bg-white rounded-full hover:bg-red-50 hover:text-red-500 transition-colors shadow-sm"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-8 overflow-y-auto flex-1 space-y-6">
              
              {selectedSession.emotionStart && selectedSession.emotionEnd && (
                <div>
                  <h4 className="text-sm font-bold text-twilight-primary uppercase tracking-wider mb-2">Emotion Shift</h4>
                  <div className="flex items-center gap-3 text-lg font-semibold text-twilight-text bg-twilight-bg p-4 rounded-2xl inline-flex">
                    <span className="capitalize">{selectedSession.emotionStart}</span>
                    <ArrowRight className="w-5 h-5 text-twilight-primary" />
                    <span className="capitalize">{selectedSession.emotionEnd}</span>
                  </div>
                </div>
              )}

              <div>
                <h4 className="text-sm font-bold text-twilight-primary uppercase tracking-wider mb-2">Summary</h4>
                <p className="text-twilight-text/80 leading-relaxed bg-twilight-bg/30 p-5 rounded-2xl border border-purple-50 whitespace-pre-wrap">
                  {selectedSession.summaryText || "No summary data received yet."}
                </p>
              </div>

              {selectedSession.actionStep && (
                <div>
                  <h4 className="text-sm font-bold text-twilight-primary uppercase tracking-wider mb-2">Recommended Action</h4>
                  <p className="text-twilight-text/80 leading-relaxed bg-green-50/50 p-5 rounded-2xl border border-green-100 whitespace-pre-wrap">
                    {selectedSession.actionStep}
                  </p>
                </div>
              )}

            </div>
          </div>
        </div>
      )}

    </div>
  );
}
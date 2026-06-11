import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { getSessionHistory, startSession, getTranscript, type SessionHistoryResponse } from "../utils/api";
import { LayoutDashboard, LogOut, Sparkles, ArrowRight, Calendar, FileText, X, TrendingUp } from "lucide-react";
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

  // Language & History State
  const [selectedLanguage, setSelectedLanguage] = useState<"EN" | "HI">("EN");
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);
  const [hoveredPoint, setHoveredPoint] = useState<{ index: number; x: number; y: number; session: SessionHistoryResponse } | null>(null);
  const [isRefreshingHistory, setIsRefreshingHistory] = useState(false);

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
      const response = await startSession(selectedLanguage);
      
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

  const handleSeeWeeklyHistory = async () => {
    setIsRefreshingHistory(true);
    setIsHistoryOpen(true);
    try {
      await loadHistory();
    } catch (error) {
      console.error("Failed to refresh history", error);
    } finally {
      setIsRefreshingHistory(false);
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
        <header className="flex flex-col xl:flex-row justify-between items-start xl:items-end gap-6 mb-10">
          <div>
            <h2 className="text-3xl font-bold text-twilight-text">Your Reflections</h2>
            <p className="text-twilight-text/70 mt-1">Review your past emotional check-ins.</p>
          </div>
          
          <div className="flex flex-wrap items-center gap-3 w-full xl:w-auto">
            {/* Language Selection Toggle */}
            <div className="flex items-center gap-1 bg-white border border-purple-100 p-1.5 rounded-2xl shadow-sm">
              <button
                type="button"
                onClick={() => setSelectedLanguage("EN")}
                className={`px-4 py-2 rounded-xl text-xs font-bold uppercase tracking-wider transition-all ${
                  selectedLanguage === "EN"
                    ? "bg-twilight-primary text-white shadow-sm"
                    : "text-twilight-text/60 hover:text-twilight-text"
                }`}
              >
                English
              </button>
              <button
                type="button"
                onClick={() => setSelectedLanguage("HI")}
                className={`px-4 py-2 rounded-xl text-xs font-bold uppercase tracking-wider transition-all ${
                  selectedLanguage === "HI"
                    ? "bg-twilight-primary text-white shadow-sm"
                    : "text-twilight-text/60 hover:text-twilight-text"
                }`}
              >
                Hindi
              </button>
            </div>

            {/* Weekly History Trigger */}
            <button 
              onClick={handleSeeWeeklyHistory}
              className="bg-white border border-purple-100 text-twilight-text hover:text-twilight-primary hover:border-twilight-primary/30 font-bold py-3 px-5 rounded-2xl shadow-sm hover:bg-purple-50/30 transition-all active:scale-[0.98] flex items-center gap-2 text-sm"
            >
              <TrendingUp className="w-4 h-4 text-twilight-primary" />
              See Weekly History
            </button>

            {/* Start Session Call-to-action */}
            <button 
              onClick={handleStartSession}
              disabled={isStartingSession}
              className="bg-twilight-primary text-white font-bold py-3.5 px-6 rounded-2xl shadow-md shadow-purple-200 hover:opacity-95 transition-all active:scale-[0.98] disabled:opacity-50 flex items-center gap-2 text-sm ml-auto xl:ml-0"
            >
              {isStartingSession ? "Waking Avatar..." : selectedLanguage === "EN" ? "Start English Session" : "Start Hindi Session"} 
              <Sparkles className="w-4 h-4" />
            </button>
          </div>
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

      {/* --- WEEKLY HISTORY MODAL --- */}
      {isHistoryOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-8">
          <div className="absolute inset-0 bg-twilight-text/20 backdrop-blur-sm transition-opacity" onClick={() => setIsHistoryOpen(false)}></div>
          
          <div className="relative w-full max-w-4xl bg-white rounded-[2rem] shadow-[0_20px_60px_rgba(0,0,0,0.15)] flex flex-col overflow-hidden animate-in fade-in zoom-in-95 duration-200 max-h-[90vh]">
            
            <div className="px-8 py-6 border-b border-purple-50 flex justify-between items-center bg-twilight-bg/30">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-twilight-primary/10 rounded-xl flex items-center justify-center">
                  <TrendingUp className="w-5 h-5 text-twilight-primary" />
                </div>
                <div>
                  <h3 className="text-xl font-bold text-twilight-text">Weekly Emotional History</h3>
                  <p className="text-sm text-twilight-text/60 mt-0.5">Track your sentiment progression over completed sessions</p>
                </div>
              </div>
              <button 
                onClick={() => setIsHistoryOpen(false)}
                className="p-2 bg-white rounded-full hover:bg-red-50 hover:text-red-500 transition-colors shadow-sm"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-8 overflow-y-auto flex-1 space-y-8">
              {isRefreshingHistory ? (
                <div className="flex flex-col items-center justify-center py-20 space-y-4">
                  <div className="relative w-12 h-12">
                    <div className="absolute inset-0 border-4 border-purple-100 rounded-full"></div>
                    <div className="absolute inset-0 border-4 border-t-twilight-primary rounded-full animate-spin"></div>
                  </div>
                  <p className="text-sm font-semibold text-twilight-text/60">Fetching latest weekly data...</p>
                </div>
              ) : (() => {
                // Filter completed sessions that have emotional start/end data
                const completedSessions = sessions
                  .filter(s => s.status === 'COMPLETED' && s.emotionStart && s.emotionEnd)
                  .slice(0, 10)
                  .reverse(); // Order chronologically (left to right)

                if (completedSessions.length === 0) {
                  return (
                    <div className="text-center py-16 bg-twilight-bg/35 rounded-3xl border border-dashed border-purple-200/60">
                      <div className="w-16 h-16 bg-white rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-sm border border-purple-50">
                        <TrendingUp className="w-8 h-8 text-twilight-primary/70" />
                      </div>
                      <h4 className="text-lg font-bold text-twilight-text mb-1">Not enough emotional data</h4>
                      <p className="text-sm text-twilight-text/60 max-w-sm mx-auto">
                        Complete at least one reflection session with emotional updates to see your weekly mood history chart!
                      </p>
                    </div>
                  );
                }

                // Mood values map
                const emotionScores: Record<string, number> = {
                  depressed: 1,
                  sad: 2,
                  anxious: 3,
                  calm: 4,
                  happy: 5
                };

                const scoreNames = ["", "Depressed", "Sad", "Anxious", "Calm", "Happy"];

                // SVG Config
                const width = 750;
                const height = 280;
                const paddingX = 80;
                const paddingY = 40;
                const graphWidth = width - paddingX * 2;
                const graphHeight = height - paddingY * 2;

                const stepX = completedSessions.length > 1 
                  ? graphWidth / (completedSessions.length - 1) 
                  : graphWidth;

                // Calculate positions for each session
                const points = completedSessions.map((session, index) => {
                  const x = paddingX + index * stepX;
                  
                  const startScore = emotionScores[session.emotionStart.toLowerCase()] || 3;
                  const endScore = emotionScores[session.emotionEnd.toLowerCase()] || 4;
                  
                  // Y coordinates mapping: 5 is at top (paddingY), 1 is at bottom (height - paddingY)
                  const startY = height - paddingY - ((startScore - 1) / 4) * graphHeight;
                  const endY = height - paddingY - ((endScore - 1) / 4) * graphHeight;
                  
                  return {
                    x,
                    startY,
                    endY,
                    startScore,
                    endScore,
                    session
                  };
                });

                // Construct SVG path for start emotions (before reflection)
                let startPath = "";
                let endPath = "";
                
                points.forEach((pt, index) => {
                  if (index === 0) {
                    startPath += `M ${pt.x} ${pt.startY}`;
                    endPath += `M ${pt.x} ${pt.endY}`;
                  } else {
                    // Start Path (using bezier curve for smoothness)
                    const prevPt = points[index - 1];
                    const cpX1 = prevPt.x + stepX / 3;
                    const cpY1 = prevPt.startY;
                    const cpX2 = pt.x - stepX / 3;
                    const cpY2 = pt.startY;
                    startPath += ` C ${cpX1} ${cpY1}, ${cpX2} ${cpY2}, ${pt.x} ${pt.startY}`;

                    // End Path
                    const cpEndX1 = prevPt.x + stepX / 3;
                    const cpEndY1 = prevPt.endY;
                    const cpEndX2 = pt.x - stepX / 3;
                    const cpEndY2 = pt.endY;
                    endPath += ` C ${cpEndX1} ${cpEndY1}, ${cpEndX2} ${cpEndY2}, ${pt.x} ${pt.endY}`;
                  }
                });

                // Area path under end line for gorgeous background gradient fill
                let endAreaPath = "";
                if (points.length > 0) {
                  endAreaPath = `${endPath} L ${points[points.length - 1].x} ${height - paddingY} L ${points[0].x} ${height - paddingY} Z`;
                }

                return (
                  <div className="space-y-8">
                    
                    {/* SVG Mood Graph Container */}
                    <div className="bg-twilight-bg/25 border border-purple-100/60 p-6 rounded-3xl relative overflow-hidden">
                      <div className="absolute top-4 left-4 flex gap-4 text-xs font-bold">
                        <div className="flex items-center gap-1.5">
                          <span className="w-2.5 h-2.5 rounded-full bg-twilight-secondary"></span>
                          <span className="text-twilight-text/70">Before reflection</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                          <span className="w-2.5 h-2.5 rounded-full bg-twilight-primary"></span>
                          <span className="text-twilight-text/70">After reflection (Mood improvement)</span>
                        </div>
                      </div>

                      <div className="w-full overflow-x-auto select-none mt-4">
                        <svg viewBox={`0 0 ${width} ${height}`} className="w-full min-w-[650px] overflow-visible">
                          <defs>
                            <linearGradient id="areaGradient" x1="0" y1="0" x2="0" y2="1">
                              <stop offset="0%" stopColor="#BDB2FF" stopOpacity="0.25"/>
                              <stop offset="100%" stopColor="#BDB2FF" stopOpacity="0"/>
                            </linearGradient>
                            <linearGradient id="verticalShiftGradient" x1="0" y1="0" x2="0" y2="1">
                              <stop offset="0%" stopColor="#BDB2FF" stopOpacity="0.8"/>
                              <stop offset="100%" stopColor="#FFC6FF" stopOpacity="0.8"/>
                            </linearGradient>
                            <filter id="glow-effect" x="-20%" y="-20%" width="140%" height="140%">
                              <feGaussianBlur stdDeviation="3" result="blur" />
                              <feComposite in="SourceGraphic" in2="blur" operator="over" />
                            </filter>
                          </defs>

                          {/* Grid Lines and Y-axis Labels */}
                          {[1, 2, 3, 4, 5].map((level) => {
                            const y = height - paddingY - ((level - 1) / 4) * graphHeight;
                            return (
                              <g key={level} className="opacity-40">
                                <line 
                                  x1={paddingX} 
                                  y1={y} 
                                  x2={width - paddingX} 
                                  y2={y} 
                                  stroke="#BDB2FF" 
                                  strokeWidth="1" 
                                  strokeDasharray="4 6" 
                                />
                                <text 
                                  x={paddingX - 15} 
                                  y={y + 4} 
                                  textAnchor="end" 
                                  className="text-[10px] font-bold fill-twilight-text"
                                >
                                  {scoreNames[level]}
                                </text>
                              </g>
                            );
                          })}

                          {/* Graph Area Gradient Under End Line */}
                          {points.length > 1 && (
                            <path d={endAreaPath} fill="url(#areaGradient)" />
                          )}

                          {/* Connection Lines (Vertical shifting inside each session) */}
                          {points.map((pt, i) => (
                            <line
                              key={`shift-${i}`}
                              x1={pt.x}
                              y1={pt.startY}
                              x2={pt.x}
                              y2={pt.endY}
                              stroke="url(#verticalShiftGradient)"
                              strokeWidth="2.5"
                              strokeDasharray="2 2"
                            />
                          ))}

                          {/* Before line */}
                          {points.length > 1 && (
                            <path 
                              d={startPath} 
                              fill="none" 
                              stroke="#FFC6FF" 
                              strokeWidth="3.5" 
                              className="drop-shadow-sm" 
                            />
                          )}

                          {/* After line */}
                          {points.length > 1 && (
                            <path 
                              d={endPath} 
                              fill="none" 
                              stroke="#BDB2FF" 
                              strokeWidth="4" 
                              filter="url(#glow-effect)" 
                            />
                          )}

                          {/* Interactive data points */}
                          {points.map((pt, i) => {
                            const isSelected = hoveredPoint?.index === i;
                            
                            return (
                              <g key={`group-${i}`}>
                                {/* Start Point (Before) */}
                                <circle
                                  cx={pt.x}
                                  cy={pt.startY}
                                  r={isSelected ? 6.5 : 4.5}
                                  fill="#FFC6FF"
                                  stroke="#FFFFFF"
                                  strokeWidth="1.5"
                                  className="cursor-pointer transition-all duration-150 hover:scale-125"
                                  onMouseEnter={() => {
                                    setHoveredPoint({
                                      index: i,
                                      x: pt.x,
                                      y: pt.startY,
                                      session: pt.session
                                    });
                                  }}
                                  onMouseLeave={() => setHoveredPoint(null)}
                                />

                                {/* End Point (After) */}
                                <circle
                                  cx={pt.x}
                                  cy={pt.endY}
                                  r={isSelected ? 8 : 5.5}
                                  fill="#BDB2FF"
                                  stroke="#FFFFFF"
                                  strokeWidth="2"
                                  className="cursor-pointer transition-all duration-150 hover:scale-125 drop-shadow-[0_2px_4px_rgba(189,178,255,0.4)]"
                                  onMouseEnter={() => {
                                    setHoveredPoint({
                                      index: i,
                                      x: pt.x,
                                      y: pt.endY,
                                      session: pt.session
                                    });
                                  }}
                                  onMouseLeave={() => setHoveredPoint(null)}
                                />

                                {/* Bottom label (Date) */}
                                <text
                                  x={pt.x}
                                  y={height - paddingY + 22}
                                  textAnchor="middle"
                                  className="text-[10px] font-bold fill-twilight-text/60"
                                >
                                  {new Date(pt.session.createdAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
                                </text>
                              </g>
                            );
                          })}
                        </svg>
                      </div>

                      {/* Tooltip Overlay */}
                      {hoveredPoint && (
                        <div 
                          className="absolute bg-white/95 backdrop-blur-md border border-purple-100 shadow-[0_10px_30px_rgba(74,78,105,0.08)] p-4 rounded-2xl w-64 text-left pointer-events-none transition-all duration-200 animate-in fade-in zoom-in-95"
                          style={{
                            left: `${Math.min(hoveredPoint.x, width - 280)}px`,
                            top: `${Math.min(hoveredPoint.y - 12, height - 160)}px`,
                            zIndex: 100
                          }}
                        >
                          <div className="flex justify-between items-center mb-2">
                            <span className="text-[10px] font-bold px-2 py-0.5 bg-twilight-bg text-twilight-text/70 rounded-full">
                              {new Date(hoveredPoint.session.createdAt).toLocaleDateString()}
                            </span>
                            <div className="flex items-center gap-1 text-[10px] font-bold text-twilight-text">
                              <span className="capitalize text-twilight-secondary">{hoveredPoint.session.emotionStart}</span>
                              <ArrowRight className="w-2.5 h-2.5 text-twilight-primary" />
                              <span className="capitalize text-twilight-primary">{hoveredPoint.session.emotionEnd}</span>
                            </div>
                          </div>
                          <h5 className="text-xs font-bold text-twilight-text mb-1 truncate">
                            {hoveredPoint.session.mainTopic || "General Check-in"}
                          </h5>
                          <p className="text-[10px] text-twilight-text/70 line-clamp-2 leading-relaxed">
                            {hoveredPoint.session.summaryText || "No summary available."}
                          </p>
                          {hoveredPoint.session.actionStep && (
                            <div className="mt-2 pt-2 border-t border-purple-50 text-[9px] text-green-700 font-semibold line-clamp-1">
                              👉 {hoveredPoint.session.actionStep}
                            </div>
                          )}
                        </div>
                      )}
                    </div>

                    {/* Detailed Cards for the plotted sessions */}
                    <div>
                      <h4 className="text-md font-bold text-twilight-text mb-4">Plotted Session Summaries</h4>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {completedSessions.map((s) => (
                          <div 
                            key={`hist-card-${s.id}`} 
                            className="bg-twilight-card border border-purple-100/60 rounded-2xl p-5 hover:shadow-md transition-all flex flex-col justify-between"
                          >
                            <div>
                              <div className="flex justify-between items-center mb-2">
                                <span className="text-[11px] font-bold text-twilight-text/60">
                                  {new Date(s.createdAt).toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                                </span>
                                <div className="flex items-center gap-1.5 text-xs font-bold text-twilight-text/80 bg-twilight-bg/60 px-2.5 py-1 rounded-full">
                                  <span className="capitalize text-pink-400">{s.emotionStart}</span>
                                  <ArrowRight className="w-3.5 h-3.5 text-twilight-primary" />
                                  <span className="capitalize text-purple-500">{s.emotionEnd}</span>
                                </div>
                              </div>
                              <h5 className="font-bold text-twilight-text text-sm mb-1.5">{s.mainTopic || "General Check-in"}</h5>
                              <p className="text-xs text-twilight-text/70 leading-relaxed mb-3">{s.summaryText}</p>
                            </div>
                            {s.actionStep && (
                              <div className="text-[11px] text-green-700 bg-green-50/50 border border-green-100 rounded-xl p-3 font-semibold mt-2">
                                <span className="font-bold text-green-800">Action Step:</span> {s.actionStep}
                              </div>
                            )}
                          </div>
                        ))}
                      </div>
                    </div>

                  </div>
                );
              })()}
            </div>

          </div>
        </div>
      )}

    </div>
  );
}
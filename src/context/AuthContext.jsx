import { createContext, useContext, useState, useEffect } from 'react';
import { initialStats, initialReports, initialAuditLogs, candidates as mockCandidates, constituencies as mockConstituencies } from '../data/mockData';
import { apiRequest } from '../services/api';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const TOKEN_KEY = 'electionToken';
  const USER_KEY = 'electionUser';
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState(initialStats);
  const [reports, setReports] = useState(initialReports);
  const [auditLogs, setAuditLogs] = useState(initialAuditLogs);
  const [votedAadhaar, setVotedAadhaar] = useState(new Set());
  const [faceEmbeddings, setFaceEmbeddings] = useState([]);
  const [faceCaptures, setFaceCaptures] = useState([]);
  const [blockedAadhaars, setBlockedAadhaars] = useState({});
  const [electionPhase, setElectionPhase] = useState('voting');
  const [currentVoter, setCurrentVoter] = useState(null);
  const [candidates, setCandidates] = useState(mockCandidates);
  const [constituencies] = useState(mockConstituencies);
  const [votes, setVotes] = useState([]);

  const partyEmojiById = {
    BJP: '🪷',
    INC: '✋',
    AAP: '🧹',
    BSP: '🐘',
    SP: '🚲',
    TMC: '🌸',
    CPM: '⚒️',
    DMK: '☀️',
    BJD: '🐚',
    NCP: '⏰',
    NOTA: '❌'
  };

  const normalizeCandidateSymbol = (candidate) => {
    const fromId = partyEmojiById[candidate.id];
    if (fromId) return fromId;

    const symbol = candidate.symbol;
    if (typeof symbol === 'string' && (symbol.startsWith('/party-symbols/') || symbol.startsWith('http'))) {
      return '🏳️';
    }
    return symbol || '🏳️';
  };

  const normalizeRole = (role) => (role || 'CITIZEN').toLowerCase();

  const normalizeUser = (rawUser) => ({
    ...rawUser,
    role: normalizeRole(rawUser.role)
  });

  const loadPublicData = async () => {
    try {
      const fetchedCandidates = await apiRequest('/public/candidates');
      if (Array.isArray(fetchedCandidates) && fetchedCandidates.length > 0) {
        setCandidates(fetchedCandidates.map(c => ({
          ...c,
          partyShort: c.partyShort || c.party,
          color: c.color || '#6b7280',
          symbol: normalizeCandidateSymbol(c)
        })));
      }
    } catch {
      // Keep fallback candidates from local mock data when backend is unavailable.
    }
  };

  const loadAdminData = async () => {
    try {
      const [statsData, reportsData, auditData, votesData] = await Promise.all([
        apiRequest('/admin/stats'),
        apiRequest('/admin/reports'),
        apiRequest('/admin/audit-logs'),
        apiRequest('/admin/votes')
      ]);

      setElectionPhase(statsData.electionPhase || 'voting');
      setStats(prev => ({
        ...prev,
        totalVotesCast: Number(statsData.totalVotesCast || 0),
        reportsSubmitted: Number(statsData.reportsSubmitted || 0)
      }));

      if (Array.isArray(reportsData)) {
        setReports(reportsData.map(r => ({
          id: `RPT${r.id}`,
          timestamp: r.createdAt,
          observerId: r.reporterId,
          observerName: r.reporterName,
          type: r.type,
          severity: r.severity,
          description: r.description,
          constituency: r.location,
          status: (r.status || 'PENDING').toLowerCase()
        })));
      }

      if (Array.isArray(auditData)) {
        setAuditLogs(auditData.map(log => ({
          id: `LOG${log.id}`,
          timestamp: log.createdAt,
          action: log.action,
          details: log.details,
          constituency: log.constituency
        })));
      }

      if (Array.isArray(votesData)) {
        setVotes(votesData.map(v => ({
          id: v.receiptId || v.id,
          receiptId: v.receiptId,
          aadhaarNumber: v.aadhaarNumber,
          voterName: v.voterName,
          voterUserId: v.voterUserId,
          candidateId: v.candidateId,
          candidateName: v.candidateName,
          constituency: v.constituency,
          timestamp: v.createdAt
        })));
      }
    } catch {
      // Keep local fallback data for admin dashboard when backend endpoints are unreachable.
    }
  };

  // Check for existing session on mount
  useEffect(() => {
    const bootstrap = async () => {
      const savedUser = localStorage.getItem(USER_KEY);
      const token = localStorage.getItem(TOKEN_KEY);

      await loadPublicData();

      if (!savedUser || !token) {
        setLoading(false);
        return;
      }

      try {
        const me = await apiRequest('/auth/me');
        const normalized = normalizeUser(me);
        setUser(normalized);
        localStorage.setItem(USER_KEY, JSON.stringify(normalized));

        if (normalized.role === 'admin' || normalized.role === 'observer') {
          await loadAdminData();
        }
      } catch {
        localStorage.removeItem(USER_KEY);
        localStorage.removeItem(TOKEN_KEY);
        setUser(null);
      }

      setLoading(false);
    };

    bootstrap();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Login function - accepts email or username
  const login = async (emailOrUsername, password) => {
    try {
      const result = await apiRequest('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email: emailOrUsername, password })
      });

      localStorage.setItem(TOKEN_KEY, result.token);
      const normalized = normalizeUser(result.user);
      setUser(normalized);
      localStorage.setItem(USER_KEY, JSON.stringify(normalized));

      if (normalized.role === 'admin' || normalized.role === 'observer') {
        await loadAdminData();
      }

      return { success: true, user: normalized };
    } catch (error) {
      return { success: false, message: error.message || 'Invalid email or password' };
    }
  };

  // Register function - creates new citizen user
  const register = async (formData) => {
    try {
      const result = await apiRequest('/auth/register', {
        method: 'POST',
        body: JSON.stringify({
          name: formData.name,
          email: formData.email,
          password: formData.password,
          role: formData.role || 'citizen',
          aadhaarNumber: formData.aadhaarNumber,
          dateOfBirth: formData.dateOfBirth,
          city: formData.city,
          state: formData.state
        })
      });

      localStorage.setItem(TOKEN_KEY, result.token);
      const normalized = normalizeUser(result.user);
      setUser(normalized);
      localStorage.setItem(USER_KEY, JSON.stringify(normalized));
      return { success: true, user: normalized };
    } catch (error) {
      return { success: false, message: error.message || 'Registration failed' };
    }
  };

  // Logout function
  const logout = () => {
    setUser(null);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(TOKEN_KEY);
  };

  // Add audit log
  const addAuditLog = (log) => {
    const newLog = {
      id: `LOG${Date.now()}`,
      timestamp: new Date().toISOString(),
      ...log
    };
    setAuditLogs(prev => [newLog, ...prev]);
  };

  // Update statistics
  const updateStats = (updates) => {
    setStats(prev => ({ ...prev, ...updates }));
  };

  // Increment a specific stat
  const incrementStat = (key, amount = 1) => {
    setStats(prev => ({
      ...prev,
      [key]: (prev[key] || 0) + amount
    }));
  };

  // Add observer report
  const addReport = (report) => {
    const addReportAsync = async () => {
      try {
        const saved = await apiRequest('/reports', {
          method: 'POST',
          body: JSON.stringify({
            type: report.type,
            location: report.location || report.constituency || 'Unknown',
            description: report.description,
            severity: report.severity || 'medium'
          })
        });

        const newReport = {
          id: `RPT${saved.id}`,
          timestamp: saved.createdAt,
          observerId: saved.reporterId,
          observerName: saved.reporterName,
          status: (saved.status || 'PENDING').toLowerCase(),
          type: saved.type,
          location: saved.location,
          description: saved.description,
          severity: saved.severity
        };

        setReports(prev => [newReport, ...prev]);
        incrementStat('reportsSubmitted');
        return newReport;
      } catch {
        const fallbackReport = {
          id: `RPT${Date.now()}`,
          timestamp: new Date().toISOString(),
          observerId: user?.id,
          observerName: user?.name,
          status: 'pending',
          ...report
        };
        setReports(prev => [fallbackReport, ...prev]);
        incrementStat('reportsSubmitted');
        return fallbackReport;
      }
    };

    return addReportAsync();
  };

  // Update report status
  const updateReportStatus = (reportId, status, resolution = null) => {
    setReports(prev => prev.map(r => 
      r.id === reportId 
        ? { ...r, status, resolution, verifiedBy: user?.id }
        : r
    ));
    
    if (status === 'resolved') {
      incrementStat('issuesResolved');
    }
  };

  // Mark Aadhaar as voted
  const markAadhaarVoted = (aadhaar) => {
    setVotedAadhaar(prev => new Set([...prev, aadhaar]));
  };

  // Check if Aadhaar has voted
  const hasAadhaarVoted = (aadhaar) => {
    return votedAadhaar.has(aadhaar);
  };

  // Add face embedding
  const addFaceEmbedding = (embedding) => {
    setFaceEmbeddings(prev => [...prev, {
      id: `FE${Date.now()}`,
      timestamp: new Date().toISOString(),
      ...embedding
    }]);
  };

  // Save face capture photo (the "folder")
  const saveFaceCapture = (capture) => {
    setFaceCaptures(prev => [...prev, {
      id: `FC${Date.now()}`,
      timestamp: new Date().toISOString(),
      ...capture
    }]);
  };

  // Delete a face capture by ID
  const deleteFaceCapture = (captureId) => {
    setFaceCaptures(prev => prev.filter(c => c.id !== captureId));
  };

  // Block an Aadhaar number for a given duration (ms)
  const blockAadhaar = (aadhaar, durationMs = 15000) => {
    const unblockAt = Date.now() + durationMs;
    setBlockedAadhaars(prev => ({ ...prev, [aadhaar]: unblockAt }));
  };

  // Check if an Aadhaar is currently blocked
  const isAadhaarBlocked = (aadhaar) => {
    const unblockAt = blockedAadhaars[aadhaar];
    if (!unblockAt) return false;
    if (Date.now() >= unblockAt) {
      // Expired — clean up
      setBlockedAadhaars(prev => {
        const copy = { ...prev };
        delete copy[aadhaar];
        return copy;
      });
      return false;
    }
    return true;
  };

  // Get remaining block time in seconds
  const getBlockRemaining = (aadhaar) => {
    const unblockAt = blockedAadhaars[aadhaar];
    if (!unblockAt) return 0;
    const remaining = Math.max(0, Math.ceil((unblockAt - Date.now()) / 1000));
    return remaining;
  };

  // Check for duplicate face using Euclidean distance on 128-d descriptors
  const checkDuplicateFace = (newDescriptor) => {
    if (!newDescriptor || faceEmbeddings.length === 0) return false;
    const THRESHOLD = 0.45; // faces with distance < 0.45 are considered the same person
    for (const stored of faceEmbeddings) {
      if (!stored.descriptor || stored.descriptor.length !== newDescriptor.length) continue;
      let sum = 0;
      for (let i = 0; i < newDescriptor.length; i++) {
        const diff = newDescriptor[i] - stored.descriptor[i];
        sum += diff * diff;
      }
      const distance = Math.sqrt(sum);
      if (distance < THRESHOLD) return true; // duplicate found
    }
    return false;
  };

  // Verify Aadhaar number against database
  const verifyAadhaar = async (aadhaarNumber) => {
    try {
      const data = await apiRequest(`/public/aadhaar/${aadhaarNumber}`);
      const dob = data.dob || data.dateOfBirth || '';
      const constituency = data.constituency || [data.city, data.state].filter(Boolean).join(', ');
      return {
        aadhaar: data.aadhaarNumber,
        name: data.name,
        dob,
        state: data.state,
        constituency
      };
    } catch {
      return null;
    }
  };

  // Cast vote - accepts object with vote details
  const castVote = async (voteData) => {
    const { aadhaarNumber, candidateId, constituency } = voteData;
    try {
      const response = await apiRequest('/votes', {
        method: 'POST',
        body: JSON.stringify({ aadhaarNumber, candidateId, constituency })
      });

      if (!response.success) {
        return { success: false, message: response.message || 'Vote could not be recorded' };
      }

      markAadhaarVoted(aadhaarNumber);
      incrementStat('totalVotesCast');

      const newTurnout = ((stats.totalVotesCast + 1) / stats.totalEligibleVoters * 100).toFixed(1);
      updateStats({ turnoutPercentage: parseFloat(newTurnout) });

      setVotes(prev => [...prev, {
        id: response.receiptId,
        aadhaarNumber,
        candidateId,
        constituency,
        timestamp: new Date().toISOString()
      }]);

      setCurrentVoter(null);
      return { success: true, receiptId: response.receiptId };
    } catch (error) {
      return { success: false, message: error.message || 'Failed to submit vote' };
    }
  };

  // Change election phase
  const changeElectionPhase = async (phase) => {
    setElectionPhase(phase);
    try {
      await apiRequest('/admin/phase', {
        method: 'PATCH',
        body: JSON.stringify({ phase })
      });
      await loadAdminData();
    } catch {
      addAuditLog({
        action: 'ELECTION_PHASE_CHANGE',
        details: `Election phase changed to ${phase}`,
        constituency: 'ALL'
      });
    }
  };

  // Check if authenticated
  const isAuthenticated = !!user;

  const value = {
    user,
    isAuthenticated,
    loading,
    login,
    logout,
    register,
    stats,
    updateStats,
    incrementStat,
    reports,
    addReport,
    updateReportStatus,
    auditLogs,
    addAuditLog,
    votedAadhaar,
    markAadhaarVoted,
    hasAadhaarVoted,
    faceEmbeddings,
    addFaceEmbedding,
    checkDuplicateFace,
    faceCaptures,
    saveFaceCapture,
    deleteFaceCapture,
    blockAadhaar,
    isAadhaarBlocked,
    getBlockRemaining,
    castVote,
    electionPhase,
    changeElectionPhase,
    verifyAadhaar,
    currentVoter,
    setCurrentVoter,
    candidates,
    constituencies,
    votes
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export default AuthContext;

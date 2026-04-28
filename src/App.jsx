import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Layout from './components/common/Layout';
import Landing from './pages/Landing';
import Login from './pages/Login';
import Register from './pages/Register';
import Verify from './pages/Verify';
import VotingBooth from './pages/VotingBooth';
import AdminDashboard from './pages/AdminDashboard';
import AnalystDashboard from './pages/AnalystDashboard';

// Protected Route Component
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, isAuthenticated } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    return <Navigate to="/" replace />;
  }
  
  return children;
};

// Auth Route - redirects if already logged in
const AuthRoute = ({ children }) => {
  const { isAuthenticated, user } = useAuth();
  
  if (isAuthenticated) {
    // Redirect based on role
    switch (user?.role) {
      case 'admin':
        return <Navigate to="/admin" replace />;
      case 'observer':
        return <Navigate to="/admin" replace />;
      case 'analyst':
        return <Navigate to="/analyst" replace />;
      default:
        return <Navigate to="/citizen/verify" replace />;
    }
  }
  
  return children;
};

function AppRoutes() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/" element={<Layout />}>
        <Route index element={<Landing />} />
        
        {/* Auth Routes */}
        <Route 
          path="login" 
          element={
            <AuthRoute>
              <Login />
            </AuthRoute>
          } 
        />
        <Route 
          path="register" 
          element={
            <AuthRoute>
              <Register />
            </AuthRoute>
          } 
        />
        
        {/* Voter Routes */}
        <Route 
          path="citizen/verify" 
          element={
            <ProtectedRoute allowedRoles={['citizen']}>
              <Verify />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="citizen/vote" 
          element={
            <ProtectedRoute allowedRoles={['citizen']}>
              <VotingBooth />
            </ProtectedRoute>
          } 
        />

        {/* Backward compatible redirects for old voter routes */}
        <Route path="verify" element={<Navigate to="/citizen/verify" replace />} />
        <Route path="vote" element={<Navigate to="/citizen/vote" replace />} />
        
        {/* Admin Routes */}
        <Route 
          path="admin/*" 
          element={
            <ProtectedRoute allowedRoles={['admin', 'observer']}>
              <AdminDashboard />
            </ProtectedRoute>
          } 
        />

        {/* Observer portal is merged into Admin portal */}
        <Route path="observer/*" element={<Navigate to="/admin" replace />} />
        
        {/* Analyst Routes */}
        <Route 
          path="analyst/*" 
          element={
            <ProtectedRoute allowedRoles={['analyst']}>
              <AnalystDashboard />
            </ProtectedRoute>
          } 
        />
        
        {/* 404 Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </Router>
  );
}

export default App;

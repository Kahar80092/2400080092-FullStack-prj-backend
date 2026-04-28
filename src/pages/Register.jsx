import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Mail, Lock, Eye, EyeOff, UserPlus, AlertCircle, CheckCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import './Auth.css';

const Register = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    aadhaarNumber: '',
    dateOfBirth: '',
    city: '',
    state: '',
    password: '',
    confirmPassword: '',
    role: 'citizen'
  });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { register } = useAuth();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const validatePassword = (password) => {
    const requirements = {
      length: password.length >= 8,
      uppercase: /[A-Z]/.test(password),
      lowercase: /[a-z]/.test(password),
      number: /[0-9]/.test(password)
    };
    return requirements;
  };

  const passwordReqs = validatePassword(formData.password);
  const isPasswordValid = Object.values(passwordReqs).every(Boolean);

  const isValidAadhaar = /^\d{12}$/.test(formData.aadhaarNumber);

  const isAdult = (() => {
    if (!formData.dateOfBirth) return false;
    const dob = new Date(formData.dateOfBirth);
    if (Number.isNaN(dob.getTime())) return false;
    const today = new Date();
    let age = today.getFullYear() - dob.getFullYear();
    const monthDelta = today.getMonth() - dob.getMonth();
    if (monthDelta < 0 || (monthDelta === 0 && today.getDate() < dob.getDate())) {
      age -= 1;
    }
    return age >= 18;
  })();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (!isPasswordValid) {
      setError('Password does not meet requirements');
      return;
    }

    if (!isValidAadhaar) {
      setError('Aadhaar number must be exactly 12 digits');
      return;
    }

    if (!isAdult) {
      setError('You must be at least 18 years old to register');
      return;
    }

    if (!formData.city.trim() || !formData.state.trim()) {
      setError('City and state are required');
      return;
    }

    setLoading(true);

    const result = await register(formData);
    
    if (result.success) {
      if (result.user.role === 'analyst') {
        navigate('/analyst');
      } else {
        navigate('/citizen/verify');
      }
    } else {
      setError(result.message);
    }
    
    setLoading(false);
  };

  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-card">
          <div className="auth-header">
            <div className="auth-logo">
              <div className="logo-chakra"></div>
            </div>
            <h1 className="auth-title">Create Account</h1>
            <p className="auth-subtitle">Join the Election Monitoring System</p>
          </div>

          <form className="auth-form" onSubmit={handleSubmit}>
            {error && (
              <div className="auth-error">
                <AlertCircle size={18} />
                <span>{error}</span>
              </div>
            )}

            <div className="form-group">
              <label htmlFor="name">Full Name</label>
              <div className="input-wrapper">
                <User size={18} className="input-icon" />
                <input
                  type="text"
                  id="name"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="Enter your full name"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="email">Email Address</label>
              <div className="input-wrapper">
                <Mail size={18} className="input-icon" />
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="Enter your email"
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="aadhaarNumber">Aadhaar Number</label>
                <div className="input-wrapper">
                  <User size={18} className="input-icon" />
                  <input
                    type="text"
                    id="aadhaarNumber"
                    name="aadhaarNumber"
                    value={formData.aadhaarNumber}
                    onChange={handleChange}
                    placeholder="12-digit Aadhaar"
                    inputMode="numeric"
                    maxLength={12}
                    required
                  />
                </div>
                {formData.aadhaarNumber && (
                  <div className={`password-match ${isValidAadhaar ? 'match' : 'no-match'}`}>
                    {isValidAadhaar ? (
                      <><CheckCircle size={14} /> Valid Aadhaar format</>
                    ) : (
                      <><AlertCircle size={14} /> Aadhaar must be 12 digits</>
                    )}
                  </div>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="dateOfBirth">Date of Birth</label>
                <div className="input-wrapper">
                  <User size={18} className="input-icon" />
                  <input
                    type="date"
                    id="dateOfBirth"
                    name="dateOfBirth"
                    value={formData.dateOfBirth}
                    onChange={handleChange}
                    required
                  />
                </div>
                {formData.dateOfBirth && (
                  <div className={`password-match ${isAdult ? 'match' : 'no-match'}`}>
                    {isAdult ? (
                      <><CheckCircle size={14} /> Age verified (18+)</>
                    ) : (
                      <><AlertCircle size={14} /> Must be 18 or older</>
                    )}
                  </div>
                )}
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="city">City</label>
                <div className="input-wrapper">
                  <User size={18} className="input-icon" />
                  <input
                    type="text"
                    id="city"
                    name="city"
                    value={formData.city}
                    onChange={handleChange}
                    placeholder="Enter your city"
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="state">State</label>
                <div className="input-wrapper">
                  <User size={18} className="input-icon" />
                  <input
                    type="text"
                    id="state"
                    name="state"
                    value={formData.state}
                    onChange={handleChange}
                    placeholder="Enter your state"
                    required
                  />
                </div>
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="role">Select Role</label>
              <div className="input-wrapper">
                <User size={18} className="input-icon" />
                <select
                  id="role"
                  name="role"
                  value={formData.role}
                  onChange={handleChange}
                  required
                >
                  <option value="citizen">Citizen / Voter</option>
                  <option value="analyst">Data Analyst</option>
                </select>
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="password">Password</label>
              <div className="input-wrapper">
                <Lock size={18} className="input-icon" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  id="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Create a password"
                  required
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              
              <div className="password-requirements">
                <div className={`req ${passwordReqs.length ? 'met' : ''}`}>
                  <CheckCircle size={14} />
                  <span>At least 8 characters</span>
                </div>
                <div className={`req ${passwordReqs.uppercase ? 'met' : ''}`}>
                  <CheckCircle size={14} />
                  <span>One uppercase letter</span>
                </div>
                <div className={`req ${passwordReqs.lowercase ? 'met' : ''}`}>
                  <CheckCircle size={14} />
                  <span>One lowercase letter</span>
                </div>
                <div className={`req ${passwordReqs.number ? 'met' : ''}`}>
                  <CheckCircle size={14} />
                  <span>One number</span>
                </div>
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm Password</label>
              <div className="input-wrapper">
                <Lock size={18} className="input-icon" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  id="confirmPassword"
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  placeholder="Confirm your password"
                  required
                />
              </div>
              {formData.confirmPassword && (
                <div className={`password-match ${formData.password === formData.confirmPassword ? 'match' : 'no-match'}`}>
                  {formData.password === formData.confirmPassword ? (
                    <><CheckCircle size={14} /> Passwords match</>
                  ) : (
                    <><AlertCircle size={14} /> Passwords do not match</>
                  )}
                </div>
              )}
            </div>

            <button 
              type="submit" 
              className="btn-auth-submit"
              disabled={loading}
            >
              {loading ? (
                <span className="loading-spinner"></span>
              ) : (
                <>
                  <UserPlus size={18} />
                  Create Account
                </>
              )}
            </button>
          </form>

          <div className="auth-footer">
            <p>Already have an account? <Link to="/login">Sign In</Link></p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;

import React, { useState } from "react";
import '../styles/AccountPage.css';
import { API_URL } from '../config';

function AccountPage() {
    const [message, setMessage] = useState('');
    const [usernameReg, setUsernameReg] = useState('');
    const [emailReg, setEmailReg] = useState('');
    const [passwordReg, setPasswordReg] = useState('');
    const [emailLogin, setEmailLogin] = useState('');
    const [passwordLogin, setPasswordLogin] = useState('');
    const [isRegistering, setIsRegistering] = useState(false);
    const [isResettingPassword, setIsResettingPassword] = useState(false);
    const [resetEmail, setResetEmail] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    // Register a new user handler
    const handleRegister = async () => {
        const response = await fetch(`${API_URL}/users/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username: usernameReg, email: emailReg, password: passwordReg }),
        });

        const text = await response.text();
        if (response.status === 400){
            window.alert(text);
        } else {
            setMessage(text);
        }
    };

    const handleLogin = async () => {
        const response = await fetch(`${API_URL}/users/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email: emailLogin, password: passwordLogin }),
        });

        const text = await response.text();
        if (response.status === 400){
            window.alert(text);
        } else {
        setMessage(text);
        }
    };

    // Password reset form
    const handlePasswordReset = async () => {
        if (newPassword !== confirmPassword) {
            window.alert("Passwords do not match");
            return;
        }

        const response = await fetch(`${API_URL}/users/reset-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email: resetEmail, newPassword }),
        });

        const text = await response.text();
        if (response.status === 400){
            window.alert(text);
        } else {
            setMessage(text);
        }
    };

    // Toggle between login and register form
    const toggleForm = () => {
        setIsRegistering(!isRegistering);
        setMessage('');
    };

    // Toggle between login and password reset form
    const togglePasswordReset = () => {
        setIsResettingPassword(!isResettingPassword);
        setMessage('');
    };

    return (
        <div className="body">
            <img src={"https://i.postimg.cc/0QQ0czTg/Logo2-Crop.png"} alt="MyApp Logo" className="logo-img-login"/>
            <div className="input-container">
                <h2>{isResettingPassword ? "Reset Password" : isRegistering ? "Create Account" : "Sign in"}</h2>
                {isResettingPassword ? (
                    <>
                        <div className="reset-container">
                            <label>Email:</label>
                            <input
                                type="text"
                                placeholder="john@example.com"
                                value={resetEmail}
                                onChange={(e) => setResetEmail(e.target.value)}
                            />
                        </div>
                        <div className="reset-container">
                            <label>New Password:</label>
                            <input
                                type="password"
                                placeholder="New password"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                            />
                        </div>
                        <div className="reset-container">
                            <label>Confirm Password:</label>
                            <input
                                type="password"
                                placeholder="Confirm password"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                            />
                        </div>
                        <button onClick={handlePasswordReset} className="submit-button">Reset Password</button>
                        <p>Remembered your password? <a href="#" onClick={togglePasswordReset}>Sign in</a></p>
                    </>
                ) : isRegistering ? (
                    <>
                        <div className="registration-container">
                            <label>Your name:</label>
                            <input
                                type="text"
                                placeholder="First and last name"
                                value={usernameReg}
                                onChange={(e) => setUsernameReg(e.target.value)}
                            />
                        </div>
                        <div className="registration-container">
                            <label>Email:</label>
                            <input
                                type="text"
                                placeholder="john@example.com"
                                value={emailReg}
                                onChange={(e) => setEmailReg(e.target.value)}
                            />
                        </div>
                        <div className="registration-container">
                            <label>Password:</label>
                            <input
                                type="password"
                                placeholder="password"
                                value={passwordReg}
                                onChange={(e) => setPasswordReg(e.target.value)}
                            />
                        </div>
                        <button onClick={handleRegister} className="submit-button">Submit</button>
                        <p>Already have an account? <a href="#" onClick={toggleForm}>Sign in </a></p>
                    </>
                ) : (
                    <>
                        <div className="login-container">
                            <label>Email:</label>
                            <input
                                type="text"
                                placeholder="john@example.com"
                                value={emailLogin}
                                onChange={(e) => setEmailLogin(e.target.value)}
                            />
                        </div>
                        <div className="login-container">
                            <label>Password:</label>
                            <input
                                type="password"
                                placeholder="password"
                                value={passwordLogin}
                                onChange={(e) => setPasswordLogin(e.target.value)}
                            />
                        </div>
                        <button onClick={handleLogin} className="submit-button">Login</button>
                        <p>New to the store? </p><a href="#" onClick={toggleForm}>Create your Store Account</a>
                        <p>Forgot your password? <a href="#" onClick={togglePasswordReset}>Reset here</a></p>
                    </>
                )}
                <p>{message}</p>
            </div>
        </div>
    );
}

export default AccountPage;
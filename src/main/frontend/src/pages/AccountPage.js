import React, { useState } from "react";
import '../styles/AccountPage.css';

function AccountPage() {
    const [message, setMessage] = useState('');
    const [usernameReg, setUsernameReg] = useState('');
    const [emailReg, setEmailReg] = useState('');
    const [passwordReg, setPasswordReg] = useState('');
    const [emailLogin, setEmailLogin] = useState('');
    const [passwordLogin, setPasswordLogin] = useState('');
    const [isRegistering, setIsRegistering] = useState(false);

    // Register a new user handler
    const handleRegister = async () => {
        const response = await fetch('/users/register', {
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
        const response = await fetch('/users/login', {
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

    // Toggle between login and register form
    const toggleForm = () => {
        setIsRegistering(!isRegistering);
        setMessage('');
    };

    return (
        <div>
            <div className="input-container">

                {isRegistering ? (
                    <>
                        <h2>Create Account</h2>
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
                        <button onClick={handleRegister} className="submit-button">Create your Store Account</button>
                        <p>Already have an account? <a href="#" onClick={toggleForm}>Sign in </a></p>
                    </>
                ) : (
                    <>
                        <h2>Sign in</h2>
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
                        <p>New to the store? </p>
                        <a href="#" onClick={toggleForm}>Create your Store Account</a>
                    </>
                )}
                <p>{message}</p>
            </div>
        </div>
    );
}

export default AccountPage;
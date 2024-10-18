import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import './Navbar.css';
import logo from './assets/images/logo512.png';

const Navbar = () => {
    // State variable to keep track of whether the mobile menu is open or closed
    const [isOpen, setIsOpen] = useState(false);
    // Function to toggle the mobile menu
    const toggleMenu = () => {
        setIsOpen(!isOpen);
    };

    return (
        <nav>
            <div className="logo">
                <Link to="/">
                    <img src={logo} alt="MyApp Logo" className="logo-img"/>
                </Link>
            </div>
            <div className={`menu ${isOpen ? 'open' : ''}`}>
                <ul>
                    <li>
                        <Link to="/">Home</Link>
                    </li>
                    <li>
                        <Link to="/page2">Page2</Link>
                    </li>
                </ul>
            </div>
            <div className="mobileMenu" onClick={toggleMenu}>
                <span></span>
                <span></span>
                <span></span>
            </div>
        </nav>
    )
}

export default Navbar;

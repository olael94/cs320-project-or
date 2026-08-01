import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import './Navbar.css';

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
                <NavLink to="/">
                    <img
                        src={'https://i.postimg.cc/0QQ0czTg/Logo2-Crop.png'}
                        alt="MyApp Logo"
                        className="logo-img"
                    />
                </NavLink>
            </div>
            <div className={`menu ${isOpen ? 'open' : ''}`}>
                <ul>
                    <li>
                        <NavLink
                            to="/"
                            end
                            className={({ isActive }) => (isActive ? 'active' : undefined)}
                        >
                            Home
                        </NavLink>
                    </li>
                    <li>
                        <NavLink
                            to="/products"
                            className={({ isActive }) => (isActive ? 'active' : undefined)}
                        >
                            Products
                        </NavLink>
                    </li>
                    <li>
                        <NavLink
                            to="/account"
                            className={({ isActive }) => (isActive ? 'active' : undefined)}
                        >
                            Account
                        </NavLink>
                    </li>
                    <li>
                        <NavLink
                            to="/cart"
                            className={({ isActive }) => (isActive ? 'active' : undefined)}
                        >
                            Cart
                        </NavLink>
                    </li>
                </ul>
            </div>
            {/* Mobile menu icon state for Hamburger animation */}
            <div className={`mobileMenu ${isOpen ? 'open' : ''}`} onClick={toggleMenu}>
                <span></span>
                <span></span>
                <span></span>
            </div>
        </nav>
    );
};

export default Navbar;

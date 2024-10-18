import React, {useState} from 'react';
import {NavLink} from 'react-router-dom';
import './Navbar.css';
import logo from '../assets/images/logo512.png';

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
                    <img src={logo} alt="MyApp Logo" className="logo-img"/>
                </NavLink>
            </div>
            <div className={`menu ${isOpen ? 'open' : ''}`}>
                <ul>
                    <li>
                        <NavLink exact to="/" activeClassName="active">Home</NavLink>
                    </li>
                    <li>
                        <NavLink to="/products" activeClassName="active">Products</NavLink>
                    </li>
                    <li>
                        <NavLink to="/cart" activeClassName="active">Cart</NavLink>
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
    )
}

export default Navbar;

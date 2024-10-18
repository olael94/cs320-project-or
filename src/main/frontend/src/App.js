import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage'; // Ensure you have a HomePage component
import ProductsPage from './pages/ProductsPage';
import CartPage from './pages/CartPage';

const App = () => {
    return (
        //BrowserRouter allows to navigate between pages
        <BrowserRouter>
            <Navbar />
            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/products" element={<ProductsPage />} />
                <Route path="/cart" element={<CartPage />} />
            </Routes>
        </BrowserRouter>
    );
};

export default App;

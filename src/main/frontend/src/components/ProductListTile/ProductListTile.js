import React from 'react';
import { Link } from 'react-router-dom';
import './ProductListTile.css';

const ProductListTile = ({ id, productName, imageURL, price }) => {
    return (
        <Link to={`/products/${id}`} className="productList-tile">
            <div className="left-container">
                <img src={imageURL} alt={productName} className="productList-image" />
            </div>
            <div className="right-container">
                <div className="productList-name">{productName}</div>
                <div className="productList-price">${price.toFixed(2)}</div>
            </div>
        </Link>
    );
};

export default ProductListTile;

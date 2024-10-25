import React from "react";
import './ProductTile.css';

const ProductTile = ({productName, imageURL, price, handleButtonClick}) => {
    return (
        <div className="product-tile">
            <img src={imageURL} alt={productName} className="product-image"/>
            <div className="product-name">{productName}</div>
            <div className="product-price">${price}</div>
        </div>
    );
};

export default ProductTile;
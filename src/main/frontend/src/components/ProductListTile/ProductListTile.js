import React from "react";
import './ProductListTile.css';

const ProductListTile = ({productName, imageURL, price, description, handleButtonClick}) => {
    return (
        <div className="productList-tile">
            <div className="left-container">
                <img src={imageURL} alt={productName} className="productList-image"/>
            </div>
            <div className="right-container">
                <div className="productList-name">{productName}</div>
                <div className="productList-price">${price}</div>
                <div className="productList-description">{description}</div>
            </div>
        </div>
    );
};

export default ProductListTile;
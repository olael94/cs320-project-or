import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { API_URL } from '../config';
import '../styles/ProductDetailPage.css';

function ProductDetailPage() {
    // Get the product ID from the URL
    const { id } = useParams();
    // use null to get 1 product not a products list
    const [product, setProduct] = useState(null);

    // Fetch the product details from the server
    useEffect(() => {
        fetch(`${API_URL}/api/products/${id}`)
            .then((res) => res.json())
            .then((data) => setProduct(data))
            .catch((error) => console.error('Error fetching product:', error));
    }, [id]);

    // Check if product is null before rendering
    if (!product) {
        return <p>Loading...</p>;
    }

    return (
        <div className="ProductDetailPage-container">
            <div className="detail-left">
                <img src={product.imageURL} alt={product.productName} className="detail-image" />
            </div>
            <div className="detail-right">
                <h1 className="detail-name">{product.productName}</h1>
                <p className="detail-price">${product.price.toFixed(2)}</p>
                <p className="detail-description">{product.description}</p>
                <p className="detail-quantity">In stock: {product.quantity}</p>
            </div>
        </div>
    );
}

export default ProductDetailPage;

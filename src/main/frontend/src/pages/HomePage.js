import React, {useEffect, useState} from 'react';
import '../styles/HomePage.css';  // Import the CSS
import {useNavigate} from 'react-router-dom';

//Imports for slider
import FeaturedSlider from "../components/FeaturedSlider/FeaturedSlider";
import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';
import ProductTile from "../components/ProductTile/ProductTile";



function HomePage() {

    // Define a state variable 'products' to store the list of products fetched from the server.
    const [products, setProducts] = useState([]);
    // Use the useEffect hook to fetch the list of products from the server when the component mounts.
    useEffect(() => {
        fetch("http://localhost:8080/products")
            .then((res) => res.json())
            .then((data) => {
                console.log("Fetched products:", data); // Check the product field names here
                setProducts(data);
            })
            .catch((error) => console.error("Error fetching products:", error));
    }, []);


    return (
        <div className="HomePage-container">
            <FeaturedSlider/>
            <div className="product-list">
                {products.map((product) => (
                    <ProductTile
                        key={product.id}
                        productName={product.productName}
                        imageURL={product.imageURL}
                        price={product.price}
                    />
                ))}
            </div>

        </div>
    );
}


export default HomePage;
import React from 'react';
import Slider from 'react-slick';
import {useNavigate} from 'react-router-dom';
import {Link} from 'react-router-dom';
import './FeaturedSlider.css';

import image1 from '../../assets/images/AirpodsBanner2.png';
import image2 from '../../assets/images/GalaxyEarbuds.png';
import image3 from '../../assets/images/PS5PRO.png';


const FeaturedSlider = () => {
    const navigate = useNavigate();

    const handleButtonClick = (link) => {
        navigate(link); // Navigate to the product link
    };


    // Array of products to be displayed in the slider
    const products = [
        {id: 1, image: image1, name: "Airpods 4", link: '/products/1', price: 100},
        {id: 2, image: image2, name: "Galaxy Earbuds", link: '/products/2', price: 200},
        {id: 3, image: image3, name: "PS5 Pro", link: '/products/3', price: 300},
    ]

    // Settings for the slider
    const settings = {
        dots: true,
        infinite: true,
        speed: 1200,
        slidesToShow: 1,
        slidesToScroll: 1,
        autoplay: true,
        autoplaySpeed: 8000,
    };

    return (
        <div className="slider-container">
            <Slider {...settings}>
                {products.map((product) => (
                    <div key={product.id} className="featured-product">
                        <img src={product.image} alt={product.name} className="product-image"/>
                        <h2 className="product-name">{product.name}</h2>
                        <button className="shop-now-button" onClick={() => handleButtonClick(product.link)}>
                            Shop Product
                        </button>
                    </div>
                ))}
            </Slider>
        </div>
    );
}

export default FeaturedSlider;
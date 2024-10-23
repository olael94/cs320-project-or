import React, {useEffect, useState} from 'react';
import '../styles/HomePage.css';  // Import the CSS
import {useNavigate} from 'react-router-dom';

//Imports for slider
import FeaturedSlider from "../components/FeaturedSlider/FeaturedSlider";
import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';



function HomePage() {
    // Define state variable 'message' to hold the response from the server, and 'setMessage' to update it.
    const [message, setMessage] = useState('');

    // Define state variables for 'firstName' and 'lastName' to capture user input.
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');

    /* This function handles the form submission. It will make an API POST request with the firstName and lastName to the server. */
    const handleSubmit = async () => {
        // Send a POST request to the '/hello/personalized' endpoint.
        const response = await fetch('/hello/personalized', {
            method: 'POST',   // Define the HTTP method as POST.
            headers: {
                'Content-Type': 'application/json',   // Specify the content type as JSON.
            },
            // Send the firstName and lastName as a JSON object in the request body.
            body: JSON.stringify({firstName, lastName}),
        });

        // Read the response as text.
        const text = await response.text();
        // Set the 'message' state to display the response text in the UI.
        setMessage(text);
    };

    return (
        <div className="HomePage-container">
            <FeaturedSlider />
            <div className="input-container">
                <div className="input-group">
                    <label>First Name:</label>
                    <input
                        type="text"
                        placeholder="John"    //Text inside the Input Box
                        value={firstName}   // Bind the input value to the firstName state
                        onChange={(e) => setFirstName(e.target.value)}    // Update firstName when user types in input
                    />
                </div>
                <div className="input-group">
                    <label>Last Name:</label>
                    <input
                        type="text"
                        placeholder="Doe"   //Text inside the Input Box
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                    />
                </div>
                {/* Button to submit the form and trigger the handleSubmit function */}
                <button onClick={handleSubmit}>Submit</button>
            </div>
            <p>{message}</p>
        </div>
    );
}


export default HomePage;
import React, { useEffect, useState } from 'react';
import './App.css';  // Import the CSS
import { useNavigate } from 'react-router-dom';

function App() {
  // Define state variable 'message' to hold the response from the server, and 'setMessage' to update it.
  const [message, setMessage] = useState('');

  // Define state variables for 'firstName' and 'lastName' to capture user input.
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  // React Router hook that allows you to navigate between different pages (routes) in your app
  const navigate = useNavigate();

  /* This function handles the form submission. It will make
  an API POST request with the firstName and lastName to the server. */
  const handleSubmit = async () => {
    // Send a POST request to the '/hello/personalized' endpoint.
    const response = await fetch('/hello/personalized', {
      method: 'POST',   // Define the HTTP method as POST.
      headers: {
        'Content-Type': 'application/json',   // Specify the content type as JSON.
      },
      // Send the firstName and lastName as a JSON object in the request body.
      body: JSON.stringify({ firstName, lastName }),
    });

    // Read the response as text.
    const text = await response.text();
    // Set the 'message' state to display the response text in the UI.
    setMessage(text);
  };

  const navigateToPage2 = () => {
    navigate('/page2');
  }

  return (
      <div className="app-container">
        <h1>Personalized Greeting</h1>
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
          <button onClick={navigateToPage2}>Page 2</button>
        </div>
        <p>{message}</p>
      </div>
  );
}


export default App;
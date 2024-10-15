import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import Page2 from './Page2';
import {BrowserRouter, Routes, Route} from 'react-router-dom';
import reportWebVitals from './reportWebVitals';

// Where React application will be rendered
const root = ReactDOM.createRoot(document.getElementById('root'));
// Where React starts rendering the application
root.render(
    // Wrapper component for running checks in development mode highlighting potential problems in an application
  <React.StrictMode>
      {/*Enables the app to have different routes (pages), so users can navigate them*/}
    <BrowserRouter>
        {/*Contains the routes (pages) the application will have*/}
      <Routes>
        <Route path="/" element={<App />} />
        <Route path="/page2" element={<Page2 />} />
      </Routes>
    </BrowserRouter>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();

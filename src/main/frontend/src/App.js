import React, {useEffect, useState} from 'react';

function App() {
  const [message, setMessage] = useState('');

  useEffect (() => {
    const fetchMessage = async () => {
      const response = await fetch ('/hello/personalized' , {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ firstName: "Ensign", lastName: "Student"})
      });
      const text = await response.text();
      setMessage(text);
    };
    fetchMessage();
  }, []);

  return(
      <div>
          <p>{message}</p>
      </div>
  );

}

export default App;
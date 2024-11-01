async function login() {
    if (!window.ethereum) {
        alert('Please install MetaMask');
        return;
    }

    try {
        const accounts = await window.ethereum.request({ method: 'eth_requestAccounts' });
        const address = accounts[0];
        const nonce = await getNonce(address);
        const message = `Wallet address: ${address} \n Nonce: ${nonce}`;
        const signature = await window.ethereum.request({ method: 'personal_sign', params: [message, address] });
        await sendLoginData(address, signature);
    } catch (error) {
        console.error('Error during MetaMask login:', error);
        // alert('MetaMask 로그인 중 문제가 발생했습니다. 다시 시도해주세요.');
    }
}

async function getNonce(address) {
    try {
        const response = await fetch(`/nonce/${address}`);
        if (!response.ok) throw new Error('Failed to fetch nonce');
        return await response.text();
    } catch (error) {
        console.error('Error fetching nonce:', error);
        alert('서버에서 인증 정보를 가져오는 데 실패했습니다.');
    }
}

async function sendLoginData(address, signature) {
    try {
        const response = await fetch('/auth/login-with-signature', {
            method: 'POST',
            headers: { 'content-type': 'application/json' },
            body: JSON.stringify({ account: address, signature })
        });

        if (response.ok) {
            alert('로그인 성공!');
            window.location.href = '/home';
        } else {
            const errorText = await response.text();
            alert(`로그인 실패: ${errorText}`);
        }
    } catch (error) {
        console.error('Error during login request:', error);
        alert('로그인 요청 중 오류가 발생했습니다.');
    }
}
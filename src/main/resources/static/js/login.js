// 팝업 열기 함수 - 팝업을 열고 MetaMask 지갑 주소와 잔액을 표시합니다.
function openPopup() {
    document.getElementById('popup').style.display = 'flex';
    document.getElementById('walletAddressText').textContent = window.ethereum.selectedAddress;
    document.getElementById('walletBalance').textContent = document.getElementById('ethBalance').textContent;
}

// 팝업 닫기 함수 - 팝업을 닫습니다.
function closePopup() {
    document.getElementById('popup').style.display = 'none';
}

// 로그인 성공 시 세션 타이머 시작 (30분 유지) - 30분 후 세션 만료 알림을 표시하고 로그인 버튼과 잔액 표시를 초기화합니다.
function startSessionTimer() {
    setTimeout(() => {
        alert("세션이 만료되었습니다. 다시 로그인 해주세요.");
        document.getElementById('loginButton').style.display = 'block';
        document.getElementById('balanceDisplay').style.display = 'none';
    }, 30 * 60 * 1000); // 30분
}

// MetaMask를 통한 로그인 함수
async function login() {
    if (!window.ethereum) {
        alert('MetaMask를 설치해주세요.');
        return;
    }

    try {
        // MetaMask 지갑 주소를 요청합니다.
        const accounts = await window.ethereum.request({ method: 'eth_requestAccounts' });
        const address = accounts[0];

        // 서버에서 nonce를 가져와서 서명할 메시지를 생성합니다.
        const nonce = await getNonce(address);
        const message = `Wallet address: ${address} \n Nonce: ${nonce}`;
        const signature = await window.ethereum.request({ method: 'personal_sign', params: [message, address] });

        // 서명한 데이터로 로그인 요청을 전송합니다.
        const response = await sendLoginData(address, signature);
        if (response.ok) {
            // 로그인 성공 시 지갑 잔액을 표시하고 로그인 버튼을 숨깁니다.
            await displayBalance(address);
            document.getElementById('loginButton').style.display = 'none';
            document.getElementById('balanceDisplay').style.display = 'flex';
            startSessionTimer(); // 30분 세션 타이머 시작
        }
    } catch (error) {
        console.error('MetaMask 로그인 중 오류:', error);
    }
}

// 서버에서 nonce를 요청하는 함수 - 로그인 시 사용자 인증을 위한 nonce를 가져옵니다.
async function getNonce(address) {
    try {
        const response = await fetch(`/nonce/${address}`);
        if (!response.ok) throw new Error('Nonce 가져오기 실패');
        return await response.text();
    } catch (error) {
        console.error('Nonce 가져오는 중 오류:', error);
    }
}

// 로그인 데이터(서명)와 함께 서버에 인증 요청을 전송하는 함수
async function sendLoginData(address, signature) {
    try {
        const response = await fetch('/auth/login-with-signature', {
            method: 'POST',
            headers: { 'content-type': 'application/json' },
            body: JSON.stringify({ account: address, signature })
        });

        if (!response.ok) {
            const errorText = await response.text();
            alert(`로그인 실패: ${errorText}`);
        }
        return response;
    } catch (error) {
        console.error('로그인 요청 중 오류 발생:', error);
    }
}

// 지갑 주소의 잔액을 가져와 화면에 표시하고 서버에 잔액을 저장하는 함수
async function displayBalance(address) {
    const web3 = new Web3(window.ethereum);
    const balance = await web3.eth.getBalance(address);
    const etherBalance = web3.utils.fromWei(balance, 'ether');
    document.getElementById('ethBalance').textContent = `${etherBalance} ETH`;

    // 서버에 잔액 정보를 저장
    await saveBalanceToServer(`${etherBalance} ETH`);
}

// 서버에 잔액 데이터를 저장하는 함수
async function saveBalanceToServer(balance) {
    try {
        const response = await fetch('/auth/save-balance', {
            method: 'POST',
            headers: { 'content-type': 'application/json' },
            body: JSON.stringify({ balance })
        });
        if (!response.ok) {
            console.error('서버에 잔액 저장 실패');
        }
    } catch (error) {
        console.error('잔액 저장 중 오류:', error);
    }
}

// 서버에서 로그인 상태를 확인하여 버튼 및 잔액 표시를 업데이트하는 함수
async function checkLoginStatus() {
    try {
        const response = await fetch('/auth/check-login-status');
        const data = await response.json();

        if (data.isLoggedIn === "true") {
            document.getElementById('loginButton').style.display = 'none';
            document.getElementById('balanceDisplay').style.display = 'flex';
            document.getElementById('ethBalance').textContent = `${data.balance}`;
        } else {
            alert("세션이 만료되었습니다. 다시 로그인 해주세요.");
            document.getElementById('loginButton').style.display = 'block';
            document.getElementById('balanceDisplay').style.display = 'none';
        }
    } catch (error) {
        console.error('로그인 상태 확인 중 오류:', error);
    }
}

// 페이지 로드 시 로그인 상태를 확인하여 UI 업데이트
window.onload = checkLoginStatus;

// 로그아웃 함수 - 세션을 종료하고 UI를 업데이트합니다.
async function logout() {
    try {
        // 로그아웃 요청을 서버에 전송
        const response = await fetch('/auth/logout', {
            method: 'POST',
            headers: { 'content-type': 'application/json' }
        });

        if (response.ok) {
            // 로그아웃 성공 시 UI 업데이트
            alert('로그아웃 성공!');
            document.getElementById('loginButton').style.display = 'block'; // 로그인 버튼 보이기
            document.getElementById('balanceDisplay').style.display = 'none'; // 잔액 표시 숨기기
            closePopup(); // 팝업 닫기
        } else {
            alert('로그아웃 실패');
        }
    } catch (error) {
        console.error('로그아웃 중 오류 발생:', error);
    }
}
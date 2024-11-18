// URL에서 쿼리 파라미터 가져오기
function getQueryParameter(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
}

// 초기화 - URL에서 전달된 이더리움 금액과 수량을 설정
document.getElementById("amount").value = getQueryParameter('ethPrice');

// 수량 확인 및 예약 요청 함수
async function checkAndReserveQuantity() {
    const itemId = getQueryParameter('itemId'); // URL에서 itemId 가져오기
    const quantity = getQueryParameter('quantity'); // URL에서 quantity 가져오기
    if (!itemId || !quantity) {
        alert("상품 ID와 수량이 필요합니다.");
        return false;
    }

    try {
        const response = await fetch(`/api/checkQuantity`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ itemId, quantity })
        });

        const result = await response.json();
        if (response.ok && result.message === "수량이 충분합니다.") {
            return true; // 수량 확인 성공
        } else {
            alert(result.message || "수량이 부족합니다.");
            return false; // 수량 부족
        }
    } catch (error) {
        console.error("수량 확인 중 오류 발생:", error);
        alert("수량 확인 중 오류가 발생했습니다.");
        return false;
    }
}

// MetaMask 연결 함수
async function connectMetaMask() {
    if (typeof window.ethereum !== 'undefined') {
        try {
            const accounts = await ethereum.request({ method: 'eth_requestAccounts' });
            document.getElementById('fromAddress').value = accounts[0];
            document.getElementById('status').innerHTML = "MetaMask connected!";
        } catch (error) {
            console.error("MetaMask 연결 중 오류:", error);
            document.getElementById('status').innerHTML = `Error: ${error.message}`;
        }
    } else {
        alert("MetaMask가 설치되어 있지 않습니다.");
    }
}

// 트랜잭션 상태 확인 함수
async function checkTransactionStatus(txHash) {
    const web3 = new Web3(window.ethereum);
    while (true) {
        const receipt = await web3.eth.getTransactionReceipt(txHash);
        if (receipt) {
            return receipt; // 트랜잭션 상태 반환
        }
        await new Promise(resolve => setTimeout(resolve, 1000)); // 1초 대기 후 재확인
    }
}

// MetaMask와 송금 트랜잭션 진행
async function sendEther() {
    const canProceed = await checkAndReserveQuantity(); // 수량 확인
    if (!canProceed) return;

    const fromAddress = document.getElementById("fromAddress").value;
    const toAddress = document.getElementById("toAddress").value;
    const amount = document.getElementById("amount").value;

    if (!fromAddress || !toAddress || !amount) {
        document.getElementById("status").innerHTML = "모든 필드를 채워 주세요";
        return;
    }

    if (typeof window.ethereum !== 'undefined') {
        try {
            const web3 = new Web3(window.ethereum);
            const transactionParameters = {
                to: toAddress,
                from: fromAddress,
                value: web3.utils.toHex(web3.utils.toWei(amount, 'ether'))
            };

            const txHash = await ethereum.request({
                method: 'eth_sendTransaction',
                params: [transactionParameters],
            });

            document.getElementById("status").innerHTML = `Transaction Hash: ${txHash}`;
            const receipt = await checkTransactionStatus(txHash);

            if (receipt && receipt.status) {
                alert("송금이 완료되었습니다!");
                await saveOrder(); // 결제 내역 저장
            } else {
                alert("트랜잭션이 실패했습니다.");
            }
        } catch (error) {
            console.error("트랜잭션 중 오류 발생:", error);
            document.getElementById("status").innerHTML = `Error: ${error.message}`;
        }
    } else {
        document.getElementById("status").innerHTML = "MetaMask가 설치되어 있지 않습니다.";
    }
}

// 결제 내역 저장 함수
async function saveOrder() {
    const userId = sessionStorage.getItem("userId");
    if (!userId) {
        alert("로그인 정보가 필요합니다. 다시 로그인해주세요.");
        return;
    }

    const itemId = getQueryParameter('itemId');
    const quantity = getQueryParameter('quantity');
    const buyerName = getQueryParameter('buyerName');
    const buyerAddress = getQueryParameter('buyerAddress');
    const buyerContact = getQueryParameter('buyerContact');

    try {
        const response = await fetch(`/api/process`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId, itemId, quantity, buyerName, buyerAddress, buyerContact })
        });

        const result = await response.json();
        if (response.ok) {
            alert("주문이 성공적으로 저장되었습니다.");
            window.location.href = "/home";
        } else {
            alert(result.error || "주문 저장 중 오류 발생");
        }
    } catch (error) {
        console.error("주문 저장 중 오류 발생:", error);
        alert("네트워크 오류가 발생했습니다.");
    }
}

// 페이지 로드 시 글쓴이 계좌 자동 입력
document.addEventListener("DOMContentLoaded", () => {
    const itemId = getQueryParameter('itemId');
    if (!itemId) return;

    fetch(`/api/getAuthorAccount/${itemId}`)
        .then(response => response.json())
        .then(data => {
            if (data.account) {
                document.getElementById("toAddress").value = data.account;
            }
        })
        .catch(error => console.error("계좌 정보 가져오기 오류:", error));
});
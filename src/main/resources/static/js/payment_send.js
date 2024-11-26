// URL에서 쿼리 파라미터 가져오기
function getQueryParameter(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
}

// 초기화 - URL에서 전달된 데이터를 설정
document.addEventListener("DOMContentLoaded", () => {
    const itemId = getQueryParameter('itemId');
    const ethPrice = getQueryParameter('ethPrice');
    document.getElementById("amount").value = ethPrice;

    // 받는 사람 주소 자동 입력
    fetch(`/api/getAuthorAccount/${itemId}`)
        .then(response => response.json())
        .then(data => {
            if (data.account) {
                document.getElementById("toAddress").value = data.account;
            }
        })
        .catch(error => console.error("받는 사람 계좌 조회 오류:", error));
});

// MetaMask 연결 함수
async function connectMetaMask() {
    if (typeof window.ethereum !== 'undefined') {
        try {
            const accounts = await ethereum.request({method: 'eth_requestAccounts'});
            document.getElementById('fromAddress').value = accounts[0];
            document.getElementById('status').innerHTML = "MetaMask connected!";
        } catch (error) {
            console.error("MetaMask 연결 오류:", error);
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

// 결제 내역 저장 함수
let isProcessing = false; // 상태 관리 변수

async function sendEther() {
    if (isProcessing) return; // 이미 요청 중이면 반환
    isProcessing = true;

    const fromAddress = document.getElementById("fromAddress").value;
    const toAddress = document.getElementById("toAddress").value;
    const amount = document.getElementById("amount").value;

    if (!fromAddress || !toAddress || !amount) {
        document.getElementById("status").innerHTML = "모든 필드를 채워 주세요";
        isProcessing = false;
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
                await saveOrder(txHash); // 트랜잭션 해시 전달
            } else {
                alert("트랜잭션이 실패했습니다.");
            }
        } catch (error) {
            console.error("트랜잭션 중 오류 발생:", error);
            document.getElementById("status").innerHTML = `Error: ${error.message}`;
        } finally {
            isProcessing = false; // 처리 완료 후 상태 해제
        }
    }
}

async function saveOrder(transactionHash) {
    const userId = sessionStorage.getItem("userId");
    const itemId = getQueryParameter('itemId');
    const quantity = getQueryParameter('quantity');
    const buyerName = getQueryParameter('buyerName');
    const buyerAddress = getQueryParameter('buyerAddress');
    const buyerContact = getQueryParameter('buyerContact');

    const requestData = {
        userId,
        items: [{itemId, quantity}],
        buyerName,
        buyerAddress,
        buyerContact,
        transactionHash,
    };

    console.log("Request Data:", requestData);

    try {
        const response = await fetch(`/api/process`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(requestData),
        });

        const result = await response.json();

        if (response.ok) {
            alert("주문이 성공적으로 저장되었습니다.");
            window.location.href = "/home";
        } else if (result.error === "이미 처리된 트랜잭션입니다.") {
            alert("중복된 결제 요청입니다. 다시 시도하지 마세요.");
            window.location.href = "/home";
        } else {
            alert(result.error || "주문 저장 중 오류 발생");
        }
    } catch (error) {
        console.error("네트워크 오류 발생:", error);
        alert("네트워크 오류가 발생했습니다.");
    }
}
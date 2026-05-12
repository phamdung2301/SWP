/**
 * Enhanced Room Table Management JavaScript
 * Combined version with all functionality
 * Version: 3.0
 */

// Global functions for room and table management - MUST be defined immediately
window.addRoom = function() {
    const modal = document.getElementById('addRoomModal');
    if (modal) {
        // Ensure footer exists and has correct buttons for Add mode
        let footer = modal.querySelector('.modal-footer');
        if (!footer) {
            const form = document.getElementById('addRoomForm');
            if (form) {
                footer = document.createElement('div');
                footer.className = 'modal-footer';
                form.appendChild(footer);
            }
        }
        
        if (footer) {
            // Check if buttons need to be reset
            const submitButton = footer.querySelector('.btn-success');
            if (!submitButton || submitButton.textContent !== '✅ Thêm phòng') {
                footer.innerHTML = '';
                
                // Add Cancel button
                const cancelButton = document.createElement('button');
                cancelButton.type = 'button';
                cancelButton.className = 'btn btn-warning';
                cancelButton.textContent = '❌ Hủy';
                cancelButton.onclick = closeAddRoomModal;
                footer.appendChild(cancelButton);
                
                // Add Submit button
                const addButton = document.createElement('button');
                addButton.type = 'button';
                addButton.className = 'btn btn-success';
                addButton.textContent = '✅ Thêm phòng';
                addButton.onclick = function(event) {
                    submitAddRoom(event);
                };
                footer.appendChild(addButton);
                
                // Force footer to be visible
                footer.style.display = 'flex';
                footer.style.visibility = 'visible';
                footer.style.opacity = '1';
                footer.style.position = 'relative';
            }
        } else {
            // Footer doesn't exist, create it
            const form = document.getElementById('addRoomForm');
            if (form) {
                footer = document.createElement('div');
                footer.className = 'modal-footer';
                
                // Add Cancel button
                const cancelButton = document.createElement('button');
                cancelButton.type = 'button';
                cancelButton.className = 'btn btn-warning';
                cancelButton.textContent = '❌ Hủy';
                cancelButton.onclick = closeAddRoomModal;
                footer.appendChild(cancelButton);
                
                // Add Submit button
                const addButton = document.createElement('button');
                addButton.type = 'button';
                addButton.className = 'btn btn-success';
                addButton.textContent = '✅ Thêm phòng';
                addButton.onclick = function(event) {
                    submitAddRoom(event);
                };
                footer.appendChild(addButton);
                
                // Force footer to be visible
                footer.style.display = 'flex';
                footer.style.visibility = 'visible';
                footer.style.opacity = '1';
                
                form.appendChild(footer);
            }
        }
        
        // Reset modal title
        const modalTitle = modal.querySelector('.modal-header h2');
        if (modalTitle) {
            modalTitle.textContent = 'Thêm phòng mới';
        }
        
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
        
        // Focus on first input
        const nameInput = document.getElementById('roomName');
        if (nameInput) {
            setTimeout(() => nameInput.focus(), 100);
        }
    } else {
        console.error('addRoomModal not found');
    }
};

window.closeAddRoomModal = function() {
    const modal = document.getElementById('addRoomModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        
        // Reset modal to "Add Room" state
        const modalTitle = modal.querySelector('.modal-header h2');
        if (modalTitle) {
            modalTitle.textContent = 'Thêm phòng mới';
        }
        
        // Ensure footer exists and reset buttons
        let footer = modal.querySelector('.modal-footer');
        if (!footer) {
            // Create footer if it doesn't exist
            const form = document.getElementById('addRoomForm');
            if (form) {
                footer = document.createElement('div');
                footer.className = 'modal-footer';
                form.appendChild(footer);
            }
        }
        
        if (footer) {
            footer.innerHTML = '';
            
            // Add Cancel button
            const cancelButton = document.createElement('button');
            cancelButton.type = 'button';
            cancelButton.className = 'btn btn-warning';
            cancelButton.textContent = '❌ Hủy';
            cancelButton.onclick = closeAddRoomModal;
            footer.appendChild(cancelButton);
            
            // Add Submit button
            const submitButton = document.createElement('button');
            submitButton.type = 'button';
            submitButton.className = 'btn btn-success';
            submitButton.textContent = '✅ Thêm phòng';
            submitButton.onclick = function(event) {
                submitAddRoom(event);
            };
            footer.appendChild(submitButton);
            
            // Force footer to be visible
            footer.style.display = 'flex';
            footer.style.visibility = 'visible';
            footer.style.opacity = '1';
            footer.style.position = 'relative';
        }
        
        // Fallback: Update existing button if footer doesn't exist
        const submitButton = modal.querySelector('.btn-success');
        if (submitButton) {
            submitButton.textContent = '✅ Thêm phòng';
            submitButton.onclick = function(event) {
                submitAddRoom(event);
            };
        }
        
        const form = document.getElementById('addRoomForm');
        if (form) {
            form.reset();
        }
    }
};

// Function to add new room to the top of the list
function addNewRoomToList(roomName, roomDescription, tableCount, totalCapacity, roomId) {
    console.log('Adding new room to list:', { roomName, roomDescription, tableCount, totalCapacity, roomId });
    
    // Find the rooms table body
    const roomsTableBody = document.querySelector('.room-table-container tbody');
    if (!roomsTableBody) {
        console.error('Rooms table body not found');
        return;
    }
    
    // Use real room ID if provided, otherwise generate temp ID
    const actualRoomId = roomId || generateTempId();
    console.log('Using room ID:', actualRoomId);
    
    // Create new row for the room
    const newRow = document.createElement('tr');
    newRow.setAttribute('data-room-id', actualRoomId);
    newRow.innerHTML = `
        <td>
            <div class="room-name">
                <strong>${escapeHtml(roomName)}</strong>
            </div>
        </td>
        <td>
            <div class="room-description">
                ${roomDescription ? escapeHtml(roomDescription) : '<em>Không có mô tả</em>'}
            </div>
        </td>
        <td>
            <span class="formatted-date" data-date="${new Date().toISOString()}">Loading...</span>
        </td>
        <td>
            <span class="table-count-badge">Tối đa ${tableCount} bàn</span>
        </td>
        <td>
            <span class="capacity-badge">Tối đa ${totalCapacity} người</span>
        </td>
        <td>
            <div class="actions">
                <button class="btn btn-warning btn-sm" onclick="editRoom('${actualRoomId}')" title="Sửa phòng">
                    Sửa
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteRoom('${actualRoomId}', event); return false;" title="Xóa phòng">
                    Xóa
                </button>
            </div>
        </td>
    `;
    
    // Insert at the top of the table
    roomsTableBody.insertBefore(newRow, roomsTableBody.firstChild);
    
    // Hide empty state if it exists
    hideRoomsEmptyState();
    
    // Format the date
    formatAllDates();
    
    // Update room count in statistics
    updateRoomCount();
    
    // Refresh pagination and go to page 1 to show new room
    refreshPagination(true, true);
    
    // Note: Room dropdown will be updated by submitAddRoom, not here to avoid duplicate
    
    console.log('✅ New room added to top of list');
}

// Function to update existing room row in place
function updateRoomRowInPlace(roomId, roomName, roomDescription, tableCount, totalCapacity) {
    console.log('Updating room row in place:', { roomId, roomName, roomDescription, tableCount, totalCapacity });
    
    // Find the row with the matching room ID
    const roomsTableBody = document.querySelector('.room-table-container tbody');
    if (!roomsTableBody) {
        console.error('Rooms table body not found');
        return;
    }
    
    // Find the row using data attribute first
    let targetRow = roomsTableBody.querySelector(`tr[data-room-id="${roomId}"]`);
    
    if (!targetRow) {
        // Fallback: Find the row by looking for the edit button with the matching roomId
        const rows = roomsTableBody.querySelectorAll('tr');
        for (let row of rows) {
            const editButton = row.querySelector('button[onclick*="editRoom"]');
            if (editButton && editButton.getAttribute('onclick').includes(roomId)) {
                targetRow = row;
                break;
            }
        }
    }
    
    if (!targetRow) {
        console.error('Room row not found for ID:', roomId);
        return;
    }
    
    // Update the row content
    const cells = targetRow.querySelectorAll('td');
    if (cells.length >= 5) {
        // Update room name
        const nameCell = cells[0];
        nameCell.innerHTML = `<div class="room-name"><strong>${escapeHtml(roomName)}</strong></div>`;
        
        // Update room description
        const descriptionCell = cells[1];
        descriptionCell.innerHTML = `<div class="room-description">${roomDescription ? escapeHtml(roomDescription) : '<em>Không có mô tả</em>'}</div>`;
        
        // Keep creation date unchanged (cells[2])
        
        // Update table count
        const tableCountCell = cells[3];
        tableCountCell.innerHTML = `<span class="table-count-badge">Tối đa ${tableCount} bàn</span>`;
        
        // Update total capacity
        const capacityCell = cells[4];
        capacityCell.innerHTML = `<span class="capacity-badge">Tối đa ${totalCapacity} người</span>`;
        
        // Keep action buttons unchanged (cells[5])
    }
    
    console.log('✅ Room row updated in place');
}

// Global variable to store room ID for deletion
let roomToDelete = null;
let tableToDelete = null;

// Function to show delete confirmation modal
window.deleteRoom = function(roomId, event) {
    // Prevent any default behavior
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }
    
    console.log('🗑️ Delete room requested:', roomId);
    console.log('RoomId type:', typeof roomId);
    console.log('RoomId length:', roomId ? roomId.length : 'null');
    
    // Find the room name from the table
    let roomsTableBody = document.querySelector('.room-table-container tbody');
    console.log('Rooms table body:', roomsTableBody);
    if (!roomsTableBody) {
        // Try alternative selector
        const roomsTable = document.querySelector('.room-table-container .table');
        console.log('Rooms table:', roomsTable);
        if (roomsTable) {
            roomsTableBody = roomsTable.querySelector('tbody');
            console.log('Table body from table:', roomsTableBody);
        }
    }
    
    if (!roomsTableBody) {
        console.error('Rooms table body not found');
        return;
    }
    
    // Find the row with the matching room ID using data attribute
    const targetRow = roomsTableBody.querySelector(`tr[data-room-id="${roomId}"]`);
    let roomName = 'Unknown Room';
    
    console.log('Looking for room with ID:', roomId);
    console.log('Target row found:', targetRow);
    
    if (targetRow) {
        // Extract room name from the first cell
        const nameCell = targetRow.querySelector('td .room-name');
        if (nameCell) {
            roomName = nameCell.textContent.trim();
            console.log('Found room name:', roomName);
        }
    } else {
        console.warn('Could not find row with data-room-id:', roomId);
        // Fallback: try the old method
        const rows = roomsTableBody.querySelectorAll('tr');
        console.log('Total rows found:', rows.length);
        
        for (let row of rows) {
            const deleteButton = row.querySelector('button[onclick*="deleteRoom"]');
            if (deleteButton) {
                const onclickAttr = deleteButton.getAttribute('onclick');
                if (onclickAttr && onclickAttr.includes(roomId)) {
                    const nameCell = row.querySelector('td .room-name');
                    if (nameCell) {
                        roomName = nameCell.textContent.trim();
                        console.log('Found room name (fallback):', roomName);
                    }
                    break;
                }
            }
        }
    }
    
    // Store room ID for later use
    roomToDelete = roomId;
    
    // Update modal content
    const roomNameElement = document.getElementById('roomNameToDelete');
    console.log('Room name element:', roomNameElement);
    if (roomNameElement) {
        roomNameElement.textContent = roomName;
        console.log('Updated room name in modal:', roomName);
    } else {
        console.error('Room name element not found!');
    }
    
    // Show modal
    const modal = document.getElementById('deleteConfirmModal');
    console.log('Modal element:', modal);
    if (modal) {
        console.log('Showing delete confirmation modal');
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
        
        // Focus on cancel button for accessibility
        const cancelBtn = modal.querySelector('.btn-secondary');
        console.log('Cancel button:', cancelBtn);
        if (cancelBtn) {
            cancelBtn.focus();
        }
    } else {
        console.error('Delete confirmation modal not found!');
        alert('Không tìm thấy modal xác nhận xóa. Vui lòng tải lại trang.');
    }
};

// Function to close delete confirmation modal
window.closeDeleteConfirmModal = function() {
    const modal = document.getElementById('deleteConfirmModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        roomToDelete = null;
    }
};

// Function to confirm and execute room deletion
window.confirmDeleteRoom = async function() {
    if (!roomToDelete) {
        console.error('No room ID to delete');
        return;
    }
    
    console.log('🗑️ Confirming deletion of room:', roomToDelete);
    
    const modal = document.getElementById('deleteConfirmModal');
    const confirmBtn = document.getElementById('confirmDeleteBtn');
    
    if (!confirmBtn) {
        console.error('Confirm delete button not found');
        return;
    }
    
    // Disable button and show loading state
    confirmBtn.disabled = true;
    confirmBtn.innerHTML = '⏳ Đang xóa...';
    
    // Show loading notification
    const loadingId = window.notificationManager?.show('Đang xóa phòng...', 'info', 'Đang xử lý', 0);
    
    try {
        // Send delete request
        const response = await fetch('roomtable', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                action: 'deleteRoom',
                roomId: roomToDelete
            })
        });
        
        console.log('Delete response status:', response.status);
        
        if (response.ok) {
            const contentType = response.headers.get('content-type');
            console.log('Content-Type:', contentType);
            
            if (contentType && contentType.includes('application/json')) {
                try {
                    const responseText = await response.text();
                    console.log('Response text:', responseText);
                    
                    let result;
                    try {
                        result = JSON.parse(responseText);
                    } catch (parseError) {
                        console.error('JSON Parse Error:', parseError);
                        const errorMatch = responseText.match(/"message":\s*"([^"]*?)"/);
                        const errorMessage = errorMatch ? errorMatch[1] : 'Lỗi không xác định từ server';
                        
                        if (loadingId) window.notificationManager?.remove(loadingId);
                        window.notificationManager?.show(errorMessage, 'error', 'Lỗi JSON');
                        return;
                    }
                    
                    console.log('Response JSON:', result);
                    
                    if (loadingId) window.notificationManager?.remove(loadingId);
                    
                    if (result.success) {
                        // Show success notification
                        window.notificationManager?.show(result.message, 'success', 'Hoàn thành');
                        
                        // Store roomToDelete before closing modal
                        const roomIdToDelete = roomToDelete;
                        console.log('🔍 Stored roomToDelete before closing modal:', roomIdToDelete);
                        
                        // Close modal
                        window.closeDeleteConfirmModal();
                        
                        // Remove room from DOM and shift rows up
                        removeRoomFromList(roomIdToDelete);
                        
                        // Remove room from dropdown in add table modal
                        removeRoomFromDropdown(roomIdToDelete);
                        
                        // Update room count
                        updateRoomCount();
                        
                        console.log('✅ Room deleted successfully');
                    } else {
                        // Show error notification
                        window.notificationManager?.show(result.message, 'error', 'Lỗi');
                        
                        // Close modal and reset button
                        window.closeDeleteConfirmModal();
                        
                        // Reset button state
                        confirmBtn.disabled = false;
                        confirmBtn.innerHTML = '🗑️ Xác nhận xóa';
                    }
                } catch (textError) {
                    console.error('Error reading response text:', textError);
                    if (loadingId) window.notificationManager?.remove(loadingId);
                    window.notificationManager?.show('Lỗi khi đọc phản hồi từ server', 'error', 'Lỗi');
                }
            } else {
                throw new Error('Response is not JSON');
            }
        } else {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    } catch (error) {
        console.error('Error deleting room:', error);
        if (loadingId) window.notificationManager?.remove(loadingId);
        window.notificationManager?.show('Có lỗi xảy ra khi xóa phòng', 'error', 'Lỗi');
    } finally {
        // Re-enable button
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = '🗑️ Xóa phòng';
    }
};

// Function to remove room from DOM after successful deletion
function removeRoomFromList(roomId) {
    console.log('Removing room from DOM:', roomId);
    
    const roomsTableBody = document.querySelector('.room-table-container tbody');
    if (!roomsTableBody) {
        console.error('Rooms table body not found');
        return;
    }
    
    // Find and remove the row using data attribute
    const rowToRemove = roomsTableBody.querySelector(`tr[data-room-id="${roomId}"]`);
    
    if (rowToRemove) {
        console.log('✅ Found row with data-room-id:', roomId);
        
        // Remove row immediately without animation for debugging
        if (rowToRemove.parentNode) {
            rowToRemove.parentNode.removeChild(rowToRemove);
            console.log('✅ Room row removed from DOM (immediate)');
            
            // Remove all tables belonging to this room
            removeTablesByRoomId(roomId);
            
            // Force update room count after removal
            updateRoomCount();
            
            // Check if this was the last room and show empty state
            const remainingRows = roomsTableBody.querySelectorAll('tr[data-room-id]');
            if (remainingRows.length === 0) {
                console.log('🏠 No rooms left, showing empty state');
                showRoomsEmptyState();
            }
            
            // Smart pagination refresh after deletion
            console.log('🔄 Calling refreshPaginationAfterDeletion (immediate)...');
            refreshPaginationAfterDeletion();
        } else {
            console.warn('⚠️ Parent node not found for row to remove:', rowToRemove);
        }
    } else {
        console.error('❌ Could not find row with data-room-id:', roomId);
        
        // Fallback: Try the old method
        console.log('Trying fallback method...');
        const rows = roomsTableBody.querySelectorAll('tr');
        let found = false;
        
        for (let row of rows) {
            const deleteButton = row.querySelector('button[onclick*="deleteRoom"]');
            if (deleteButton) {
                const onclickAttr = deleteButton.getAttribute('onclick');
                console.log('Checking row with onclick:', onclickAttr);
                
                if (onclickAttr && (
                    onclickAttr.includes(roomId) || 
                    onclickAttr.includes(`'${roomId}'`) ||
                    onclickAttr.includes(`"${roomId}"`)
                )) {
                    console.log('✅ Found matching row for room ID:', roomId);
                    found = true;
                    
                    // Add fade out animation
                    row.style.transition = 'opacity 0.3s ease-out, transform 0.3s ease-out';
                    row.style.opacity = '0';
                    row.style.transform = 'translateX(-100%)';
                    
                    // Remove row after animation
                    setTimeout(() => {
                        if (row.parentNode) {
                            row.parentNode.removeChild(row);
                            console.log('✅ Room row removed from DOM (fallback)');
                            
                            // Remove all tables belonging to this room
                            removeTablesByRoomId(roomId);
                            
                            // Force update room count after removal
                            setTimeout(() => {
                                updateRoomCount();
                            }, 100);
                        }
                    }, 300);
                    break;
                }
            }
        }
        
        // Second fallback: Try to find by room name from modal
        if (!found) {
            console.log('Trying second fallback method by room name...');
            const roomNameElement = document.getElementById('roomNameToDelete');
            if (roomNameElement) {
                const roomNameToDelete = roomNameElement.textContent.trim();
                console.log('Looking for room with name:', roomNameToDelete);
                
                for (let row of rows) {
                    const nameCell = row.querySelector('.room-name');
                    if (nameCell) {
                        const roomName = nameCell.textContent.trim();
                        console.log('Checking room name:', roomName);
                        
                        if (roomName === roomNameToDelete) {
                            console.log('✅ Found matching row by room name:', roomName);
                            found = true;
                            
                            // Add fade out animation
                            row.style.transition = 'opacity 0.3s ease-out, transform 0.3s ease-out';
                            row.style.opacity = '0';
                            row.style.transform = 'translateX(-100%)';
                            
                            // Remove row after animation
                            setTimeout(() => {
                                if (row.parentNode) {
                                    row.parentNode.removeChild(row);
                                    console.log('✅ Room row removed from DOM (name fallback)');
                                    
                                    // Remove all tables belonging to this room
                                    removeTablesByRoomId(roomId);
                                    
                                    // Force update room count after removal
                                    setTimeout(() => {
                                        updateRoomCount();
                                    }, 100);
                                }
                            }, 300);
                            break;
                        }
                    }
                }
            }
        }
        
        if (!found) {
            console.error('❌ Could not find row with any method');
            console.log('Available room IDs in table:');
            rows.forEach((row, index) => {
                const dataRoomId = row.getAttribute('data-room-id');
                const deleteButton = row.querySelector('button[onclick*="deleteRoom"]');
                if (deleteButton) {
                    const onclickAttr = deleteButton.getAttribute('onclick');
                    console.log(`Row ${index}: data-room-id="${dataRoomId}", onclick="${onclickAttr}"`);
                }
            });
            
            // Don't reload page, just show error notification
            console.log('Could not find row to delete, but not reloading page');
            window.notificationManager?.show('Không thể tìm thấy phòng để xóa trong danh sách', 'warning', 'Cảnh báo');
        }
    }
}

// Helper function to get current table room from DOM   
function getCurrentTableRoomFromDOM(tableId) {
    console.log('🔍 Getting current table room from DOM for table:', tableId);
    
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) {
        console.log('❌ Tables container not found');
        return '';
    }
    
    const tableBody = tablesContainer.querySelector('.table tbody');
    if (!tableBody) {
        console.log('❌ Tables table not found');
        return '';
    }
    
    const rows = tableBody.querySelectorAll('tr');
    for (let row of rows) {
        const editButton = row.querySelector('button[onclick*="editTable"]');
        if (editButton) {
            const onclickAttr = editButton.getAttribute('onclick');
            const tableIdMatch = onclickAttr.match(/editTable\('([^']+)'\)/);
            if (tableIdMatch && tableIdMatch[1] === tableId) {
                // Found the table row, get room from 3rd column
                const roomCell = row.querySelector('td:nth-child(3)');
                if (roomCell) {
                    const roomBadge = roomCell.querySelector('.room-badge');
                    if (roomBadge) {
                        const roomName = roomBadge.textContent.trim();
                        console.log('✅ Found current table room:', roomName);
                        return roomName;
                    } else {
                        console.log('✅ Table has no room assigned');
                        return '';
                    }
                }
            }
        }
    }
    
    console.log('❌ Table room not found for table:', tableId);
    return '';
}

// Helper function to get current table capacity from DOM
function getCurrentTableCapacityFromDOM(tableId) {
    console.log('🔍 Getting current table capacity from DOM for table:', tableId);
    
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) {
        console.log('❌ Tables container not found');
        return 0;
    }
    
    const tableBody = tablesContainer.querySelector('.table tbody');
    if (!tableBody) {
        console.log('❌ Tables table not found');
        return 0;
    }
    
    const rows = tableBody.querySelectorAll('tr');
    for (let row of rows) {
        const editButton = row.querySelector('button[onclick*="editTable"]');
        if (editButton) {
            const onclickAttr = editButton.getAttribute('onclick');
            const tableIdMatch = onclickAttr.match(/editTable\('([^']+)'\)/);
            if (tableIdMatch && tableIdMatch[1] === tableId) {
                // Found the table row, get capacity from 4th column
                const capacityCell = row.querySelector('td:nth-child(4)');
                if (capacityCell) {
                    const capacityText = capacityCell.textContent.trim();
                    const capacityMatch = capacityText.match(/(\d+)/);
                    const capacity = capacityMatch ? parseInt(capacityMatch[1]) : 0;
                    console.log('✅ Found current table capacity:', capacity, 'from text:', capacityText);
                    return capacity;
                }
            }
        }
    }
    
    console.log('❌ Table capacity not found for table:', tableId);
    return 0;
}

// Helper function to get room ID by room name
function getRoomIdByName(roomName) {
    console.log('🔍 Getting room ID by name:', roomName);
    
    const roomsContainer = document.querySelectorAll('.room-table-container')[0];
    if (!roomsContainer) {
        console.log('❌ Rooms container not found');
        return null;
    }
    
    const table = roomsContainer.querySelector('.table tbody');
    if (!table) {
        console.log('❌ Rooms table not found');
        return null;
    }
    
    const rows = table.querySelectorAll('tr');
    for (let row of rows) {
        const nameCell = row.querySelector('td:first-child');
        if (nameCell && nameCell.textContent.trim() === roomName) {
            const editButton = row.querySelector('button[onclick*="editRoom"]');
            if (editButton) {
                const onclickAttr = editButton.getAttribute('onclick');
                const roomIdMatch = onclickAttr.match(/editRoom\('([^']+)'\)/);
                if (roomIdMatch) {
                    console.log('✅ Found room ID:', roomIdMatch[1], 'for room:', roomName);
                    return roomIdMatch[1];
                }
            }
        }
    }
    
    console.log('❌ Room ID not found for room:', roomName);
    return null;
}

// Helper function to check if table name already exists in the same room (excluding current table being edited)
function isTableNameDuplicateInRoomForEdit(tableName, roomId, currentTableId) {
    console.log('🔍 Checking for duplicate table name in room (edit mode):', tableName, 'room:', roomId, 'excluding table:', currentTableId);
    
    const roomsContainer = document.querySelectorAll('.room-table-container')[0];
    if (!roomsContainer) {
        console.log('❌ Rooms container not found');
        return false;
    }
    
    const table = roomsContainer.querySelector('.table tbody');
    if (!table) {
        console.log('❌ Rooms table not found');
        return false;
    }
    
    // Get room info to match by name
    const room = getRoomById(roomId);
    if (!room) {
        console.log('❌ Room not found for ID:', roomId);
        return false;
    }
    
    // Check tables in the same room
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) {
        console.log('❌ Tables container not found');
        return false;
    }
    
    const tablesTable = tablesContainer.querySelector('.table tbody');
    if (!tablesTable) {
        console.log('❌ Tables table not found');
        return false;
    }
    
    const rows = tablesTable.querySelectorAll('tr');
    for (let row of rows) {
        const nameCell = row.querySelector('td:nth-child(2)'); // Table name column
        const roomCell = row.querySelector('td:nth-child(3)'); // Room column
        const editButton = row.querySelector('button[onclick*="editTable"]');
        
        if (nameCell && roomCell && editButton) {
            const existingName = nameCell.textContent.trim();
            const roomBadge = roomCell.querySelector('.room-badge');
            const onclickAttr = editButton.getAttribute('onclick');
            const tableIdMatch = onclickAttr.match(/editTable\('([^']+)'\)/);
            
            if (roomBadge && tableIdMatch) {
                const roomName = roomBadge.textContent.trim();
                const existingTableId = tableIdMatch[1];
                
                // Skip the current table being edited
                if (existingTableId === currentTableId) {
                    continue;
                }
                
                // Check if name matches other tables in the same room
                if (roomName === room.name && existingName.toLowerCase() === tableName.toLowerCase()) {
                    console.log('❌ Duplicate table name found:', existingName, 'in room:', room.name, 'table:', existingTableId);
                    return true;
                }
            }
        }
    }
    
    console.log('✅ No duplicate table name found in room');
    return false;
}

// Helper function to check if table number already exists in the same room (excluding current table being edited)
function isTableNumberDuplicateInRoomForEdit(tableNumber, roomId, currentTableId) {
    console.log('🔍 Checking for duplicate table number in room (edit mode):', tableNumber, 'room:', roomId, 'excluding table:', currentTableId);
    
    const roomsContainer = document.querySelectorAll('.room-table-container')[0];
    if (!roomsContainer) {
        console.log('❌ Rooms container not found');
        return false;
    }
    
    const table = roomsContainer.querySelector('.table tbody');
    if (!table) {
        console.log('❌ Rooms table not found');
        return false;
    }
    
    // Get room info to match by name
    const room = getRoomById(roomId);
    if (!room) {
        console.log('❌ Room not found for ID:', roomId);
        return false;
    }
    
    // Check tables in the same room
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) {
        console.log('❌ Tables container not found');
        return false;
    }
    
    const tablesTable = tablesContainer.querySelector('.table tbody');
    if (!tablesTable) {
        console.log('❌ Tables table not found');
        return false;
    }
    
    const rows = tablesTable.querySelectorAll('tr');
    for (let row of rows) {
        const numberCell = row.querySelector('td:nth-child(1)'); // Table number column
        const roomCell = row.querySelector('td:nth-child(3)'); // Room column
        const editButton = row.querySelector('button[onclick*="editTable"]');
        
        if (numberCell && roomCell && editButton) {
            const existingNumber = numberCell.textContent.trim();
            const roomBadge = roomCell.querySelector('.room-badge');
            const onclickAttr = editButton.getAttribute('onclick');
            const tableIdMatch = onclickAttr.match(/editTable\('([^']+)'\)/);
            
            if (roomBadge && tableIdMatch) {
                const roomName = roomBadge.textContent.trim();
                const existingTableId = tableIdMatch[1];
                
                // Skip the current table being edited
                if (existingTableId === currentTableId) {
                    continue;
                }
                
                // Check if number matches other tables in the same room
                if (roomName === room.name && existingNumber.toLowerCase() === tableNumber.toLowerCase()) {
                    console.log('❌ Duplicate table number found:', existingNumber, 'in room:', room.name, 'table:', existingTableId);
                    return true;
                }
            }
        }
    }
    
    console.log('✅ No duplicate table number found in room');
    return false;
}

// Helper function to check if table name already exists in the same room
function isTableNameDuplicateInRoom(tableName, roomId) {
    console.log('🔍 Checking for duplicate table name in room:', tableName, 'room:', roomId);
    
    const roomsContainer = document.querySelectorAll('.room-table-container')[0];
    if (!roomsContainer) {
        console.log('❌ Rooms container not found');
        return false;
    }
    
    const table = roomsContainer.querySelector('.table tbody');
    if (!table) {
        console.log('❌ Rooms table not found');
        return false;
    }
    
    // Get room info to match by name
    const room = getRoomById(roomId);
    if (!room) {
        console.log('❌ Room not found for ID:', roomId);
        return false;
    }
    
    // Check tables in the same room
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) {
        console.log('❌ Tables container not found');
        return false;
    }
    
    const tablesTable = tablesContainer.querySelector('.table tbody');
    if (!tablesTable) {
        console.log('❌ Tables table not found');
        return false;
    }
    
    const rows = tablesTable.querySelectorAll('tr');
    for (let row of rows) {
        const nameCell = row.querySelector('td:nth-child(2)'); // Table name column
        const roomCell = row.querySelector('td:nth-child(3)'); // Room column
        
        if (nameCell && roomCell) {
            const existingName = nameCell.textContent.trim();
            const roomBadge = roomCell.querySelector('.room-badge');
            
            if (roomBadge) {
                const roomName = roomBadge.textContent.trim();
                if (roomName === room.name && existingName.toLowerCase() === tableName.toLowerCase()) {
                    console.log('❌ Duplicate table name found:', existingName, 'in room:', room.name);
                    return true;
                }
            }
        }
    }
    
    console.log('✅ No duplicate table name found in room');
    return false;
}

// Helper function to check if table number already exists in the same room
function isTableNumberDuplicateInRoom(tableNumber, roomId) {
    console.log('🔍 Checking for duplicate table number in room:', tableNumber, 'room:', roomId);
    
    const roomsContainer = document.querySelectorAll('.room-table-container')[0];
    if (!roomsContainer) {
        console.log('❌ Rooms container not found');
        return false;
    }
    
    const table = roomsContainer.querySelector('.table tbody');
    if (!table) {
        console.log('❌ Rooms table not found');
        return false;
    }
    
    // Get room info to match by name
    const room = getRoomById(roomId);
    if (!room) {
        console.log('❌ Room not found for ID:', roomId);
        return false;
    }
    
    // Check tables in the same room
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) {
        console.log('❌ Tables container not found');
        return false;
    }
    
    const tablesTable = tablesContainer.querySelector('.table tbody');
    if (!tablesTable) {
        console.log('❌ Tables table not found');
        return false;
    }
    
    const rows = tablesTable.querySelectorAll('tr');
    for (let row of rows) {
        const numberCell = row.querySelector('td:nth-child(1)'); // Table number column
        const roomCell = row.querySelector('td:nth-child(3)'); // Room column
        
        if (numberCell && roomCell) {
            const existingNumber = numberCell.textContent.trim();
            const roomBadge = roomCell.querySelector('.room-badge');
            
            if (roomBadge) {
                const roomName = roomBadge.textContent.trim();
                if (roomName === room.name && existingNumber.toLowerCase() === tableNumber.toLowerCase()) {
                    console.log('❌ Duplicate table number found:', existingNumber, 'in room:', room.name);
                    return true;
                }
            }
        }
    }
    
    console.log('✅ No duplicate table number found in room');
    return false;
}

// Helper function to check if room name already exists (excluding current room being edited)
function isRoomNameDuplicateForEdit(roomName, currentRoomId) {
    console.log('🔍 Checking for duplicate room name (edit mode):', roomName, 'excluding room:', currentRoomId);
    
    const roomsContainer = document.querySelectorAll('.room-table-container')[0];
    if (!roomsContainer) {
        console.log('❌ Rooms container not found');
        return false;
    }
    
    const table = roomsContainer.querySelector('.table tbody');
    if (!table) {
        console.log('❌ Rooms table not found');
        return false;
    }
    
    const rows = table.querySelectorAll('tr');
    for (let row of rows) {
        const nameCell = row.querySelector('td:first-child');
        const editButton = row.querySelector('button[onclick*="editRoom"]');
        
        if (nameCell && editButton) {
            const existingName = nameCell.textContent.trim();
            const onclickAttr = editButton.getAttribute('onclick');
            const roomIdMatch = onclickAttr.match(/editRoom\('([^']+)'\)/);
            
            if (roomIdMatch) {
                const existingRoomId = roomIdMatch[1];
                // Skip the current room being edited
                if (existingRoomId === currentRoomId) {
                    continue;
                }
                
                // Check if name matches other rooms
                if (existingName.toLowerCase() === roomName.toLowerCase()) {
                    console.log('❌ Duplicate room name found:', existingName, 'in room:', existingRoomId);
                    return true;
                }
            }
        }
    }
    
    console.log('✅ No duplicate room name found');
    return false;
}

// Helper function to check if room name already exists
function isRoomNameDuplicate(roomName) {
    console.log('🔍 Checking for duplicate room name:', roomName);
    
    const roomsContainer = document.querySelectorAll('.room-table-container')[0];
    if (!roomsContainer) {
        console.log('❌ Rooms container not found');
        return false;
    }
    
    const table = roomsContainer.querySelector('.table tbody');
    if (!table) {
        console.log('❌ Rooms table not found');
        return false;
    }
    
    const rows = table.querySelectorAll('tr');
    for (let row of rows) {
        const nameCell = row.querySelector('td:first-child');
        if (nameCell) {
            const existingName = nameCell.textContent.trim();
            if (existingName.toLowerCase() === roomName.toLowerCase()) {
                console.log('❌ Duplicate room name found:', existingName);
                return true;
            }
        }
    }
    
    console.log('✅ No duplicate room name found');
    return false;
}

// Helper function to escape HTML
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Helper function to generate temporary ID (will be replaced with real ID on reload)
function generateTempId() {
    return 'temp-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
}

// Function to update room count in statistics
function updateRoomCount() {
    console.log('Updating room count...');
    
    // Check if we're in empty state
    const roomsContainer = document.querySelector('.room-table-container');
    const emptyState = roomsContainer?.querySelector('.empty-state');
    const isEmptyStateVisible = emptyState && emptyState.style.display !== 'none';
    
    if (isEmptyStateVisible) {
        // We're in empty state, set count to 0
        const roomCountElement = document.querySelector('.stat-card .stat-number');
        if (roomCountElement) {
            const oldCount = roomCountElement.textContent;
            roomCountElement.textContent = '0';
            console.log(`Room count updated (empty state): ${oldCount} → 0`);
        }
        return;
    }
    
    // Normal case: count rows in tbody
    const roomsTableBody = document.querySelector('.room-table-container tbody');
    if (roomsTableBody) {
        const roomCount = roomsTableBody.children.length;
        console.log('Current room count:', roomCount);
        
        const roomCountElement = document.querySelector('.stat-card .stat-number');
        if (roomCountElement) {
            const oldCount = roomCountElement.textContent;
            roomCountElement.textContent = roomCount;
            console.log(`Room count updated: ${oldCount} → ${roomCount}`);
        } else {
            console.error('Room count element not found');
        }
    } else {
        console.error('Rooms table body not found for count update');
    }
}

// Function to update table count in statistics
function updateTableCount() {
    console.log('Updating table count...');
    
    // Check if we're in empty state
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    const emptyState = tablesContainer?.querySelector('.empty-state');
    const isEmptyStateVisible = emptyState && emptyState.style.display !== 'none';
    
    if (isEmptyStateVisible) {
        // We're in empty state, set counts to 0
        const tableCountElements = document.querySelectorAll('.stat-card .stat-number');
        
        // Update total tables count (second stat card)
        if (tableCountElements.length >= 2) {
            const totalTablesElement = tableCountElements[1];
            const oldCount = totalTablesElement.textContent;
            totalTablesElement.textContent = '0';
            console.log(`Total tables count updated (empty state): ${oldCount} → 0`);
        }
        
        // Update available tables count (third stat card)
        if (tableCountElements.length >= 3) {
            const availableTablesElement = tableCountElements[2];
            const oldCount = availableTablesElement.textContent;
            availableTablesElement.textContent = '0';
            console.log(`Available tables count updated (empty state): ${oldCount} → 0`);
        }
        
        // Update occupied tables count (fourth stat card)
        if (tableCountElements.length >= 4) {
            const occupiedTablesElement = tableCountElements[3];
            const oldCount = occupiedTablesElement.textContent;
            occupiedTablesElement.textContent = '0';
            console.log(`Occupied tables count updated (empty state): ${oldCount} → 0`);
        }
        
        return;
    }
    
    // Normal case: count rows in tbody
    const tablesTableBody = tablesContainer?.querySelector('tbody');
    if (tablesTableBody) {
        const tableCount = tablesTableBody.children.length;
        console.log('Current table count:', tableCount);
        
        // Update total tables count (second stat card)
        const tableCountElements = document.querySelectorAll('.stat-card .stat-number');
        if (tableCountElements.length >= 2) {
            const totalTablesElement = tableCountElements[1];
            const oldCount = totalTablesElement.textContent;
            totalTablesElement.textContent = tableCount;
            console.log(`Total tables count updated: ${oldCount} → ${tableCount}`);
        } else {
            console.error('Table count element not found');
        }
        
        // Update available tables count (third stat card)
        const availableTables = tablesTableBody.querySelectorAll('tr .status.available').length;
        if (tableCountElements.length >= 3) {
            const availableTablesElement = tableCountElements[2];
            const oldAvailableCount = availableTablesElement.textContent;
            availableTablesElement.textContent = availableTables;
            console.log(`Available tables count updated: ${oldAvailableCount} → ${availableTables}`);
        }
        
        // Update occupied tables count (fourth stat card)
        const occupiedTables = tablesTableBody.querySelectorAll('tr .status.occupied').length;
        if (tableCountElements.length >= 4) {
            const occupiedTablesElement = tableCountElements[3];
            const oldOccupiedCount = occupiedTablesElement.textContent;
            occupiedTablesElement.textContent = occupiedTables;
            console.log(`Occupied tables count updated: ${oldOccupiedCount} → ${occupiedTables}`);
        }
    } else {
        console.error('Tables table body not found for count update');
    }
}

window.addTable = function() {
    const modal = document.getElementById('addTableModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
        
        // Reset modal to "Add Table" state
        const modalTitle = modal.querySelector('.modal-header h2');
        if (modalTitle) {
            modalTitle.textContent = 'Thêm bàn mới';
        }
        
        const submitButton = modal.querySelector('.btn-success');
        if (submitButton) {
            submitButton.textContent = '✅ Thêm bàn';
            submitButton.onclick = function(event) {
                submitAddTable(event);
            };
        }
        
        const form = document.getElementById('addTableForm');
        if (form) {
            form.reset();
        }
        
        // Hide room limits info initially
        const roomLimitsInfo = modal.querySelector('.room-limits-info');
        if (roomLimitsInfo) {
            roomLimitsInfo.classList.remove('show');
        }
    } else {
        console.error('addTableModal not found');
    }
};

window.closeAddTableModal = function() {
    const modal = document.getElementById('addTableModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        
        // Reset modal to "Add Table" state
        const modalTitle = modal.querySelector('.modal-header h2');
        if (modalTitle) {
            modalTitle.textContent = 'Thêm bàn mới';
        }
        
        const submitButton = modal.querySelector('.btn-success');
        if (submitButton) {
            submitButton.textContent = '✓ Thêm bàn';
            submitButton.onclick = function(event) {
                submitAddTable(event);
            };
        }
        
        const form = document.getElementById('addTableForm');
        if (form) {
            form.reset();
            form.classList.remove('loading');
        }
        
        // Hide room limits info
        const roomLimitsInfo = modal.querySelector('.room-limits-info');
        if (roomLimitsInfo) {
            roomLimitsInfo.classList.remove('show');
        }
        
        // Reset room dropdown
        const roomSelect = document.getElementById('roomId');
        if (roomSelect) {
            roomSelect.value = '';
        }
        
        // Let CSS handle the height, don't override
        const modalContent = modal.querySelector('.modal-content');
        if (modalContent) {
            modalContent.style.height = '';
            modalContent.style.maxHeight = '';
        }
    }
};

window.closeEditTableModal = function() {
    const modal = document.getElementById('editTableModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        
        // Reset form
        const form = document.getElementById('editTableForm');
        if (form) {
            form.reset();
        }
    }
};

// Function to add new table to the top of the list
function addNewTableToList(tableNumber, tableName, roomName, capacity, status, tableId) {
    console.log('Adding new table to list:', { tableNumber, tableName, roomName, capacity, status, tableId });
    
    // Find the tables table body
    const tablesTableBody = document.querySelectorAll('.room-table-container')[1]?.querySelector('tbody');
    if (!tablesTableBody) {
        console.error('Tables table body not found');
        return;
    }
    
    // Use real table ID if provided, otherwise generate temp ID
    const actualTableId = tableId || generateTempId();
    console.log('Using table ID:', actualTableId);
    
    // Get room ID if room name is provided
    const actualRoomId = roomName ? getRoomIdByName(roomName) : null;
    console.log('Using room ID:', actualRoomId);
    
    // Create new row for the table
    const newRow = document.createElement('tr');
    newRow.setAttribute('data-table-id', actualTableId);
    if (actualRoomId) {
        newRow.setAttribute('data-room-id', actualRoomId);
    }
    newRow.innerHTML = `
        <td>
            <span class="table-number">${escapeHtml(tableNumber)}</span>
        </td>
        <td>
            <span class="table-name">${escapeHtml(tableName)}</span>
        </td>
        <td>
            <span class="room-badge">${roomName ? escapeHtml(roomName) : '<span class="no-room">Chưa phân phòng</span>'}</span>
        </td>
        <td>
            <span class="capacity-badge">${capacity} người</span>
        </td>
        <td>
            <span class="status ${status.toLowerCase()}">
                ${getStatusText(status)}
            </span>
        </td>
        <td>
            <span class="formatted-date" data-date="${new Date().toISOString()}">Loading...</span>
        </td>
        <td>
            <div class="actions">
                <button class="btn btn-warning btn-sm" onclick="editTable('${actualTableId}')" title="Sửa bàn">
                    Sửa
                </button>
                <button class="btn btn-success btn-sm" onclick="viewTableHistory('${actualTableId}')" title="Xem lịch sử">
                    Lịch sử
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteTable('${actualTableId}', event); return false;" title="Xóa bàn">
                    Xóa
                </button>
            </div>
        </td>
    `;
    
    // Insert at the top of the table
    tablesTableBody.insertBefore(newRow, tablesTableBody.firstChild);
    
    // Format the date
    formatAllDates();
    
    // Update table count in statistics
    updateTableCount();
    
    // Hide empty state if it exists
    hideTablesEmptyState();
    
    // Refresh pagination and go to page 1 to show new table
    refreshPagination(false, true);
    
    // Update room limits info if a room was selected
    if (roomName && roomName.trim() !== '') {
        // Find the room ID from the room name
        const roomSelect = document.getElementById('roomId');
        if (roomSelect) {
            for (let option of roomSelect.options) {
                if (option.textContent.includes(roomName)) {
                    // Update room limits info for the selected room
                    setTimeout(() => {
                        window.updateRoomLimits();
                    }, 100); // Small delay to ensure DOM is updated
                    break;
                }
            }
        }
    }
    
    console.log('✅ New table added to top of list');
}

// Function to update existing table row in place
function updateTableRowInPlace(tableId, tableNumber, tableName, roomName, capacity, status) {
    console.log('Updating table row in place:', { tableId, tableNumber, tableName, roomName, capacity, status });
    
    // Find the row with the matching table ID
    const tablesTableBody = document.querySelectorAll('.room-table-container')[1]?.querySelector('tbody');
    if (!tablesTableBody) {
        console.error('Tables table body not found');
        return;
    }
    
    // Find the row using data attribute first
    let targetRow = tablesTableBody.querySelector(`tr[data-table-id="${tableId}"]`);
    
    if (!targetRow) {
        // Fallback: Find the row by looking for the edit button with the exact matching tableId
        const rows = tablesTableBody.querySelectorAll('tr');
        for (let row of rows) {
            const editButton = row.querySelector('button[onclick*="editTable"]');
            if (editButton) {
                const onclickAttr = editButton.getAttribute('onclick');
                const tableIdMatch = onclickAttr.match(/editTable\('([^']+)'\)/);
                if (tableIdMatch && tableIdMatch[1] === tableId) {
                    targetRow = row;
                    break;
                }
            }
        }
    }
    
    if (!targetRow) {
        console.error('Table row not found for ID:', tableId);
        return;
    }
    
    // Update the row content
    const cells = targetRow.querySelectorAll('td');
    if (cells.length >= 6) {
        // Update table number
        const numberCell = cells[0];
        numberCell.innerHTML = `<span class="table-number">${escapeHtml(tableNumber)}</span>`;
        
        // Update table name
        const nameCell = cells[1];
        nameCell.innerHTML = `<span class="table-name">${escapeHtml(tableName)}</span>`;
        
        // Update room
        const roomCell = cells[2];
        roomCell.innerHTML = `<span class="room-badge">${roomName ? escapeHtml(roomName) : '<span class="no-room">Chưa phân phòng</span>'}</span>`;
        
        // Update capacity
        const capacityCell = cells[3];
        capacityCell.innerHTML = `<span class="capacity-badge">${capacity} người</span>`;
        
        // Update status
        const statusCell = cells[4];
        statusCell.innerHTML = `<span class="status ${status.toLowerCase()}">${getStatusText(status)}</span>`;
        
        // Keep creation date unchanged (cells[5])
        // Keep action buttons unchanged (cells[6])
    }
    
    console.log('✅ Table row updated in place');
    
    // Update room limits info after table update
    setTimeout(() => {
        window.updateRoomLimits();
    }, 100); // Small delay to ensure DOM is updated
}

// Helper function to get status text
function getStatusText(status) {
    switch (status) {
        case 'Available': return 'Trống';
        case 'Occupied': return 'Đang sử dụng';
        case 'Reserved': return 'Đã đặt';
        case 'Maintenance': return 'Bảo trì';
        default: return status;
    }
}

window.submitAddRoom = async function(event) {
    // Prevent default form submission
    if (event) {
        event.preventDefault();
    }
    
    const form = document.getElementById('addRoomForm');
    if (!form) {
        console.error('addRoomForm not found');
        return;
    }
    
    // Get form values
    const nameInput = document.getElementById('roomName');
    const descriptionInput = document.getElementById('roomDescription');
    const tableCountInput = document.getElementById('roomTableCount');
    const totalCapacityInput = document.getElementById('roomTotalCapacity');
    
    const name = nameInput ? nameInput.value : '';
    const description = descriptionInput ? descriptionInput.value : '';
    const tableCount = tableCountInput ? tableCountInput.value : '';
    const totalCapacity = totalCapacityInput ? totalCapacityInput.value : '';

    if (!name || name.trim() === '') {
        window.notificationManager?.show('Vui lòng nhập tên phòng', 'warning', 'Thiếu thông tin');
        if (nameInput) nameInput.focus();
        return;
    }

    if (name.trim().length > 100) {
        window.notificationManager?.show('Tên phòng không được vượt quá 100 ký tự', 'error', 'Lỗi nhập liệu');
        if (nameInput) nameInput.focus();
        return;
    }

    // Check for duplicate room name
    if (isRoomNameDuplicate(name.trim())) {
        window.notificationManager?.show('Tên phòng đã tồn tại. Vui lòng chọn tên khác', 'error', 'Tên phòng trùng lặp');
        if (nameInput) {
            nameInput.focus();
            nameInput.select(); // Select the text for easy editing
        }
        return;
    }

    if (!tableCount || tableCount.trim() === '') {
        window.notificationManager?.show('Vui lòng nhập số lượng bàn', 'warning', 'Thiếu thông tin');
        if (tableCountInput) tableCountInput.focus();
        return;
    }

    if (!totalCapacity || totalCapacity.trim() === '') {
        window.notificationManager?.show('Vui lòng nhập tổng sức chứa', 'warning', 'Thiếu thông tin');
        if (totalCapacityInput) totalCapacityInput.focus();
        return;
    }

    const tableCountNum = parseInt(tableCount.trim());
    if (isNaN(tableCountNum) || tableCountNum < 0 || tableCountNum > 50) {
        window.notificationManager?.show('Số lượng bàn phải từ 0 đến 50', 'error', 'Lỗi nhập liệu');
        if (tableCountInput) tableCountInput.focus();
        return;
    }

    const totalCapacityNum = parseInt(totalCapacity.trim());
    if (isNaN(totalCapacityNum) || totalCapacityNum < 1 || totalCapacityNum > 1000) {
        window.notificationManager?.show('Tổng sức chứa phải từ 1 đến 1000', 'error', 'Lỗi nhập liệu');
        if (totalCapacityInput) totalCapacityInput.focus();
        return;
    }

    // Show loading notification
    const loadingId = window.notificationManager?.show('Đang thêm phòng...', 'info', 'Đang xử lý', 0);
    
    // Add loading class to form
    form.classList.add('loading');
    
    try {
        // Create JSON data instead of FormData
        const requestData = {
            action: 'addRoom',
            roomName: name.trim(),
            roomDescription: description ? description.trim() : '',
            roomTableCount: tableCountNum,
            roomTotalCapacity: totalCapacityNum
        };
        
        // Debug: Log request data
        console.log('Request data:', requestData);
        
        // Submit via AJAX with JSON
        const response = await fetch('roomtable', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(requestData)
        });
        
        console.log('Response status:', response.status);
        console.log('Response headers:', response.headers);
        
        if (response.ok) {
            const contentType = response.headers.get('content-type');
            console.log('Content-Type:', contentType);
            
            if (contentType && contentType.includes('application/json')) {
                const result = await response.json();
                console.log('Response JSON:', result);
                
                // Remove loading notification
                if (loadingId) window.notificationManager?.remove(loadingId);
                
                if (result.success) {
                    // Show success notification
                    window.notificationManager?.show(result.message, 'success', 'Hoàn thành');
                    
                    // Reset form
                    form.reset();
                    
                    // Close modal
                    window.closeAddRoomModal();
                    
                    // Add new room to the top of the list without reloading
                    console.log('🔄 Adding new room to top of list...');
                    console.log('Room ID from server:', result.roomId);
                    addNewRoomToList(name.trim(), description ? description.trim() : '', tableCountNum, totalCapacityNum, result.roomId);
                    
                    // Update room dropdown in add table modal
                    updateRoomDropdownWithNewRoom(result.roomId, name.trim(), tableCountNum, totalCapacityNum);
                } else {
                    // Show error notification
                    window.notificationManager?.show(result.message, 'error', 'Lỗi');
                }
            } else {
                throw new Error('Response is not JSON');
            }
        } else {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
    } catch (error) {
        console.error('Error adding room:', error);
        
        // Remove loading notification
        if (loadingId) window.notificationManager?.remove(loadingId);
        
        // Show error notification
        window.notificationManager?.show('Có lỗi xảy ra khi thêm phòng', 'error', 'Lỗi');
    } finally {
        // Remove loading class
        form.classList.remove('loading');
    }
};

window.submitAddTable = async function(event) {
    // Prevent default form submission
    if (event) {
        event.preventDefault();
    }
    
    const form = document.getElementById('addTableForm');
    if (!form) {
        console.error('addTableForm not found');
        return;
    }
    
    // Get form values
    const tableNumberInput = document.getElementById('tableNumber');
    const tableNameInput = document.getElementById('tableName');
    const capacityInput = document.getElementById('capacity');
    const roomIdInput = document.getElementById('roomId');
    
    const tableNumber = tableNumberInput ? tableNumberInput.value : '';
    const tableName = tableNameInput ? tableNameInput.value : '';
    const capacity = capacityInput ? capacityInput.value : '';
    const roomId = roomIdInput ? roomIdInput.value : '';

    if (!tableNumber || tableNumber.trim() === '') {
        window.notificationManager?.show('Vui lòng nhập số bàn', 'warning', 'Thiếu thông tin');
        if (tableNumberInput) tableNumberInput.focus();
        return;
    }

    if (!tableName || tableName.trim() === '') {
        window.notificationManager?.show('Vui lòng nhập tên bàn', 'warning', 'Thiếu thông tin');
        if (tableNameInput) tableNameInput.focus();
        return;
    }

    if (!capacity || capacity.trim() === '') {
        window.notificationManager?.show('Vui lòng nhập sức chứa', 'warning', 'Thiếu thông tin');
        if (capacityInput) capacityInput.focus();
        return;
    }

    if (tableNumber.trim().length > 50) {
        window.notificationManager?.show('Số bàn không được vượt quá 50 ký tự', 'error', 'Lỗi nhập liệu');
        if (tableNumberInput) tableNumberInput.focus();
        return;
    }

    if (tableName.trim().length > 100) {
        window.notificationManager?.show('Tên bàn không được vượt quá 100 ký tự', 'error', 'Lỗi nhập liệu');
        if (tableNameInput) tableNameInput.focus();
        return;
    }

    const capacityNum = parseInt(capacity.trim());
    if (isNaN(capacityNum) || capacityNum < 1 || capacityNum > 20) {
        window.notificationManager?.show('Sức chứa phải từ 1 đến 20 người', 'error', 'Lỗi nhập liệu');
        if (capacityInput) capacityInput.focus();
        return;
    }

    // Check for duplicate table name and number in the same room
    if (roomId && roomId.trim() !== '') {
        if (isTableNameDuplicateInRoom(tableName.trim(), roomId)) {
            window.notificationManager?.show('Tên bàn đã tồn tại trong phòng này. Vui lòng chọn tên khác', 'error', 'Tên bàn trùng lặp');
            if (tableNameInput) {
                tableNameInput.focus();
                tableNameInput.select();
            }
            return;
        }
        
        if (isTableNumberDuplicateInRoom(tableNumber.trim(), roomId)) {
            window.notificationManager?.show('Số bàn đã tồn tại trong phòng này. Vui lòng chọn số khác', 'error', 'Số bàn trùng lặp');
            if (tableNumberInput) {
                tableNumberInput.focus();
                tableNumberInput.select();
            }
                    return;
                }
            }
            
    // Validate room limits if room is selected
    if (roomId && roomId.trim() !== '') {
        // Additional validation using the new function
        if (!validateCapacityAgainstRoomLimits(null, 'roomId', 'capacity')) {
                    return;
                }
        
        // Skip additional validation - handled by validateCapacityAgainstRoomLimits
    }

    // Show loading notification
    const loadingId = window.notificationManager?.show('Đang thêm bàn...', 'info', 'Đang xử lý', 0);
    
    // Add loading class to form
    form.classList.add('loading');
    
    try {
        // Sanitize strings to prevent JSON parsing errors
        const sanitizeString = (str) => {
            if (!str) return '';
            return str.trim()
                .replace(/[\u0000-\u001F\u007F-\u009F]/g, '') // Remove control characters
                .replace(/[\u2000-\u200F\u2028-\u202F\u205F-\u206F]/g, ' ') // Replace various spaces with regular space
                .replace(/\s+/g, ' ') // Replace multiple spaces with single space
                .trim();
        };
        
        // Create JSON payload with sanitized data
        const payload = {
            action: 'addTable',
            tableNumber: sanitizeString(tableNumber),
            tableName: sanitizeString(tableName),
            capacity: sanitizeString(capacity)
        };
        
        if (roomId && roomId.trim() !== '') {
            payload.roomId = sanitizeString(roomId);
        }
        
        // Debug: Log payload
        console.log('JSON payload:', payload);
        
        // Submit via AJAX with JSON
        const response = await fetch('roomtable', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(payload)
        });
        
        console.log('Response status:', response.status);
        console.log('Response headers:', response.headers);
        
        if (response.ok) {
            const contentType = response.headers.get('content-type');
            console.log('Content-Type:', contentType);
            
            if (contentType && contentType.includes('application/json')) {
                try {
                    const responseText = await response.text();
                    console.log('Response text:', responseText);
                    
                    // Try to parse JSON with error handling
                    let result;
                    try {
                        result = JSON.parse(responseText);
                    } catch (parseError) {
                        console.error('JSON parse error:', parseError);
                        console.error('Raw response:', responseText);
                        
                        // Try to extract error message from raw text
                        const errorMatch = responseText.match(/"message"\s*:\s*"([^"]*)"/);
                        const errorMessage = errorMatch ? errorMatch[1] : 'Lỗi không xác định';
                        
                        result = {
                            success: false,
                            message: errorMessage
                        };
                    }
                    
                    console.log('Response JSON:', result);
                    
                    // Remove loading notification
                    if (loadingId) window.notificationManager?.remove(loadingId);
                    
                    if (result.success) {
                        // Show success notification
                        window.notificationManager?.show(result.message, 'success', 'Hoàn thành');
                        
                        // Reset form
                        form.reset();
                        
                        // Close modal
                        window.closeAddTableModal();
                        
                        // Update table (add or edit) without reloading
                        console.log('🔄 Updating table list...');
                        
                        // Get room name for display
                        let roomName = '';
                        if (roomId && roomId.trim() !== '') {
                            const selectedRoom = getRoomById(roomId);
                            if (selectedRoom) {
                                roomName = selectedRoom.name;
                            }
                        }
                        
                        // Get status from form or default to Available
                        const statusInput = document.getElementById('status');
                        const status = statusInput ? statusInput.value : 'Available';
                        
                        // Check if this is add or edit mode based on tableId existence
                        if (typeof tableId !== 'undefined' && tableId) {
                            // Edit mode: update existing table
                            updateTableRowInPlace(
                                tableId,
                                tableNumber.trim(), 
                                tableName.trim(), 
                                roomName, 
                                capacityNum, 
                                status
                            );
                        } else {
                            // Add mode: add new table to list with real tableId from server
                            addNewTableToList(
                                tableNumber.trim(), 
                                tableName.trim(), 
                                roomName, 
                                capacityNum, 
                                status,
                                result.tableId // Use real tableId from server
                            );
                            
                            // Update table count
                            updateTableCount();
                            
                            // Update room limits
                            window.updateRoomLimits();
                        }
                    } else {
                        // Show error notification
                        window.notificationManager?.show(result.message, 'error', 'Lỗi');
                        
                        // Don't close modal on error - let user fix the issue
                        // But ensure form is not in loading state
                        form.classList.remove('loading');
                    }
                } catch (error) {
                    console.error('Error processing response:', error);
                    
                    // Remove loading notification
                    if (loadingId) window.notificationManager?.remove(loadingId);
                    
                    // Show error notification
                    window.notificationManager?.show('Có lỗi xảy ra khi xử lý phản hồi từ server', 'error', 'Lỗi');
                    
                    // Ensure form is not in loading state
                    form.classList.remove('loading');
                }
            } else {
                // Try to get response as text to see what we're getting
                const responseText = await response.text();
                console.log('Response text:', responseText);
                throw new Error('Response is not JSON: ' + responseText.substring(0, 200));
            }
        } else {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
    } catch (error) {
        console.error('Error adding table:', error);
        
        // Remove loading notification
        if (loadingId) window.notificationManager?.remove(loadingId);
        
        // Show error notification
        window.notificationManager?.show('Có lỗi xảy ra khi thêm bàn', 'error', 'Lỗi');
        
        // Ensure form is not in loading state
        form.classList.remove('loading');
    } finally {
        // Remove loading class
        form.classList.remove('loading');
    }
};

window.editRoom = function(roomId) {
    console.log('🔄 Edit room:', roomId);
    
    // Get room data from DOM
    const room = getRoomById(roomId);
    if (!room) {
        window.notificationManager?.show('Không tìm thấy thông tin phòng', 'error', 'Lỗi');
        return;
    }
    
    console.log('📋 Room data:', room);
    
    // Get current room data from the table row
    const roomsContainer = document.querySelectorAll('.room-table-container')[0];
    if (!roomsContainer) {
        window.notificationManager?.show('Không tìm thấy bảng phòng', 'error', 'Lỗi');
        return;
    }
    
    const table = roomsContainer.querySelector('.table tbody');
    if (!table) {
        window.notificationManager?.show('Không tìm thấy dữ liệu phòng', 'error', 'Lỗi');
        return;
    }
    
    const rows = table.querySelectorAll('tr');
    let roomRow = null;
    
    for (let row of rows) {
        const editButton = row.querySelector('button[onclick*="editRoom"]');
        if (editButton) {
            const onclickAttr = editButton.getAttribute('onclick');
            const roomIdMatch = onclickAttr.match(/editRoom\('([^']+)'\)/);
            if (roomIdMatch && roomIdMatch[1] === roomId) {
                roomRow = row;
                break;
            }
        }
    }
    
    if (!roomRow) {
        window.notificationManager?.show('Không tìm thấy hàng phòng', 'error', 'Lỗi');
        return;
    }
    
    // Extract data from the row
    const nameCell = roomRow.querySelector('td:first-child');
    const descriptionCell = roomRow.querySelector('td:nth-child(2)');
    const tableCountCell = roomRow.querySelector('td:nth-child(4)');
    const totalCapacityCell = roomRow.querySelector('td:nth-child(5)');
    
    if (!nameCell || !tableCountCell || !totalCapacityCell) {
        window.notificationManager?.show('Dữ liệu phòng không đầy đủ', 'error', 'Lỗi');
        return;
    }
    
    const roomName = nameCell.textContent.trim();
    const roomDescription = descriptionCell ? descriptionCell.textContent.trim() : '';
    const tableCountText = tableCountCell.textContent.trim();
    const totalCapacityText = totalCapacityCell.textContent.trim();
    
    // Extract numbers from text
    const tableCountMatch = tableCountText.match(/Tối đa (\d+) bàn/);
    const totalCapacityMatch = totalCapacityText.match(/Tối đa (\d+) người/);
    
    const tableCount = tableCountMatch ? parseInt(tableCountMatch[1]) : 0;
    const totalCapacity = totalCapacityMatch ? parseInt(totalCapacityMatch[1]) : 0;
    
    console.log('📊 Extracted data:', {
        name: roomName,
        description: roomDescription,
        tableCount: tableCount,
        totalCapacity: totalCapacity
    });
    
    // Show edit modal
    showEditRoomModal(roomId, roomName, roomDescription, tableCount, totalCapacity);
};

// Function to show edit room modal with pre-filled data
function showEditRoomModal(roomId, roomName, roomDescription, tableCount, totalCapacity) {
    console.log('🔄 Showing edit room modal for:', roomId);
    
    // Get the add room modal
    const modal = document.getElementById('addRoomModal');
    if (!modal) {
        window.notificationManager?.show('Không tìm thấy modal chỉnh sửa', 'error', 'Lỗi');
        return;
    }
    
    // Update modal title
    const modalTitle = modal.querySelector('.modal-header h2');
    if (modalTitle) {
        modalTitle.textContent = 'Chỉnh sửa phòng';
    }
    
    // Pre-fill form fields
    const nameInput = document.getElementById('roomName');
    const descriptionInput = document.getElementById('roomDescription');
    const tableCountInput = document.getElementById('roomTableCount');
    const totalCapacityInput = document.getElementById('roomTotalCapacity');
    
    if (nameInput) nameInput.value = roomName;
    if (descriptionInput) descriptionInput.value = roomDescription;
    if (tableCountInput) tableCountInput.value = tableCount;
    if (totalCapacityInput) totalCapacityInput.value = totalCapacity;
    
    // Ensure footer exists and is visible
    let footer = modal.querySelector('.modal-footer');
    if (!footer) {
        // Create footer if it doesn't exist
        const form = document.getElementById('addRoomForm');
        if (form) {
            footer = document.createElement('div');
            footer.className = 'modal-footer';
            form.appendChild(footer);
        }
    }
    
    // Update footer buttons
    if (footer) {
        // Clear existing buttons
        footer.innerHTML = '';
        
        // Add Cancel button
        const cancelButton = document.createElement('button');
        cancelButton.type = 'button';
        cancelButton.className = 'btn btn-warning';
        cancelButton.textContent = '❌ Hủy';
        cancelButton.onclick = closeAddRoomModal;
        footer.appendChild(cancelButton);
        
        // Add Update button
        const updateButton = document.createElement('button');
        updateButton.type = 'button';
        updateButton.className = 'btn btn-success';
        updateButton.textContent = '✅ Cập nhật';
        updateButton.onclick = function(event) {
            submitEditRoom(event, roomId);
        };
        footer.appendChild(updateButton);
        
        // Force footer to be visible
        footer.style.display = 'flex';
        footer.style.visibility = 'visible';
        footer.style.opacity = '1';
        footer.style.position = 'relative';
    } else {
        // Footer doesn't exist, create it
        const form = document.getElementById('addRoomForm');
        if (form) {
            footer = document.createElement('div');
            footer.className = 'modal-footer';
            
            // Add Cancel button
            const cancelButton = document.createElement('button');
            cancelButton.type = 'button';
            cancelButton.className = 'btn btn-warning';
            cancelButton.textContent = '❌ Hủy';
            cancelButton.onclick = closeAddRoomModal;
            footer.appendChild(cancelButton);
            
            // Add Update button
            const updateButton = document.createElement('button');
            updateButton.type = 'button';
            updateButton.className = 'btn btn-success';
            updateButton.textContent = '✅ Cập nhật';
            updateButton.onclick = function(event) {
                submitEditRoom(event, roomId);
            };
            footer.appendChild(updateButton);
            
            // Force footer to be visible
            footer.style.display = 'flex';
            footer.style.visibility = 'visible';
            footer.style.opacity = '1';
            
            form.appendChild(footer);
        }
    }
    
    // Update submit button (fallback for old code)
    const submitButton = modal.querySelector('.btn-success');
    if (submitButton && submitButton.textContent !== '✅ Cập nhật') {
        submitButton.textContent = '✅ Cập nhật';
        submitButton.onclick = function(event) {
            submitEditRoom(event, roomId);
        };
    }
    
    // Show modal
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
    
    // Focus on first input
    if (nameInput) nameInput.focus();
}

// Function to submit edit room
window.submitEditRoom = async function(event, roomId) {
    if (event) {
        event.preventDefault();
    }
    
    console.log('🔄 Submitting edit room:', roomId);
    
    const form = document.getElementById('addRoomForm');
    if (!form) {
        console.error('addRoomForm not found');
        return;
    }
    
    // Get form values
    const nameInput = document.getElementById('roomName');
    const descriptionInput = document.getElementById('roomDescription');
    const tableCountInput = document.getElementById('roomTableCount');
    const totalCapacityInput = document.getElementById('roomTotalCapacity');
    
    const name = nameInput ? nameInput.value : '';
    const description = descriptionInput ? descriptionInput.value : '';
    const tableCount = tableCountInput ? tableCountInput.value : '';
    const totalCapacity = totalCapacityInput ? totalCapacityInput.value : '';

    // Validation
    if (!name || name.trim() === '') {
        window.notificationManager?.show('Vui lòng nhập tên phòng', 'warning', 'Thiếu thông tin');
        if (nameInput) nameInput.focus();
        return;
    }

    if (name.trim().length > 100) {
        window.notificationManager?.show('Tên phòng không được vượt quá 100 ký tự', 'error', 'Lỗi nhập liệu');
        if (nameInput) nameInput.focus();
        return;
    }

    // Check for duplicate room name (excluding current room being edited)
    if (isRoomNameDuplicateForEdit(name.trim(), roomId)) {
        window.notificationManager?.show('Tên phòng đã tồn tại. Vui lòng chọn tên khác', 'error', 'Tên phòng trùng lặp');
        if (nameInput) {
            nameInput.focus();
            nameInput.select(); // Select the text for easy editing
        }
        return;
    }

    if (!tableCount || tableCount.trim() === '') {
        window.notificationManager?.show('Vui lòng nhập số lượng bàn', 'warning', 'Thiếu thông tin');
        if (tableCountInput) tableCountInput.focus();
        return;
    }

    if (!totalCapacity || totalCapacity.trim() === '') {
        window.notificationManager?.show('Vui lòng nhập tổng sức chứa', 'warning', 'Thiếu thông tin');
        if (totalCapacityInput) totalCapacityInput.focus();
        return;
    }

    const tableCountNum = parseInt(tableCount.trim());
    if (isNaN(tableCountNum) || tableCountNum < 0 || tableCountNum > 50) {
        window.notificationManager?.show('Số lượng bàn phải từ 0 đến 50', 'error', 'Lỗi nhập liệu');
        if (tableCountInput) tableCountInput.focus();
        return;
    }

    const totalCapacityNum = parseInt(totalCapacity.trim());
    if (isNaN(totalCapacityNum) || totalCapacityNum < 1 || totalCapacityNum > 1000) {
        window.notificationManager?.show('Tổng sức chứa phải từ 1 đến 1000', 'error', 'Lỗi nhập liệu');
        if (totalCapacityInput) totalCapacityInput.focus();
        return;
    }

    // Show loading notification
    const loadingId = window.notificationManager?.show('Đang cập nhật phòng...', 'info', 'Đang xử lý', 0);
    
    // Add loading class to form
    form.classList.add('loading');
    
    try {
        // Sanitize strings to prevent JSON parsing errors
        const sanitizeString = (str) => {
            if (!str) return '';
            return str.trim()
                .replace(/[\u0000-\u001F\u007F-\u009F]/g, '') // Remove control characters
                .replace(/[\u2000-\u200F\u2028-\u202F\u205F-\u206F]/g, ' ') // Replace various spaces with regular space
                .replace(/\s+/g, ' ') // Replace multiple spaces with single space
                .trim();
        };
        
        const requestData = {
            action: 'editRoom',
            roomId: roomId,
            roomName: sanitizeString(name),
            roomDescription: sanitizeString(description),
            roomTableCount: tableCountNum,
            roomTotalCapacity: totalCapacityNum
        };
        
        console.log('Request data:', requestData);
        
        const response = await fetch('roomtable', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(requestData)
        });
        
        console.log('Response status:', response.status);
        
        if (response.ok) {
            const contentType = response.headers.get('content-type');
            console.log('Content-Type:', contentType);
            
            if (contentType && contentType.includes('application/json')) {
                try {
                    const responseText = await response.text();
                    console.log('Response text:', responseText);
                    
                    // Try to parse JSON with error handling
                    let result;
                    try {
                        result = JSON.parse(responseText);
                    } catch (parseError) {
                        console.error('JSON Parse Error:', parseError);
                        console.error('Response text that failed to parse:', responseText);
                        
                        // Try to extract error message from malformed JSON
                        const errorMatch = responseText.match(/"message":\s*"([^"]*?)"/);
                        const errorMessage = errorMatch ? errorMatch[1] : 'Lỗi không xác định từ server';
                        
                        if (loadingId) window.notificationManager?.remove(loadingId);
                        window.notificationManager?.show(errorMessage, 'error', 'Lỗi JSON');
                        return;
                    }
                    
                    console.log('Response JSON:', result);
                    
                    if (loadingId) window.notificationManager?.remove(loadingId);
                    
                    if (result.success) {
                        window.notificationManager?.show(result.message, 'success', 'Hoàn thành');
                        form.reset();
                        window.closeAddRoomModal();
                        
                        // Update the existing row in place without reloading
                        console.log('🔄 Updating room row in place...');
                        updateRoomRowInPlace(roomId, name.trim(), description ? description.trim() : '', tableCountNum, totalCapacityNum);
                        
                        // Update room dropdown in add table modal
                        updateRoomDropdownWithUpdatedRoom(roomId, name.trim(), tableCountNum, totalCapacityNum);
                    } else {
                        window.notificationManager?.show(result.message, 'error', 'Lỗi');
                    }
                } catch (textError) {
                    console.error('Error reading response text:', textError);
                    if (loadingId) window.notificationManager?.remove(loadingId);
                    window.notificationManager?.show('Lỗi khi đọc phản hồi từ server', 'error', 'Lỗi');
                }
            } else {
                throw new Error('Response is not JSON');
            }
        } else {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    } catch (error) {
        console.error('Error updating room:', error);
        if (loadingId) window.notificationManager?.remove(loadingId);
        window.notificationManager?.show('Có lỗi xảy ra khi cập nhật phòng', 'error', 'Lỗi');
    } finally {
        form.classList.remove('loading');
    }
};

window.editTable = function(tableId) {
    console.log('🔄 Edit table:', tableId);
    
    // Get table data from DOM
    const table = getTableById(tableId);
    if (!table) {
        window.notificationManager?.show('Không tìm thấy thông tin bàn', 'error', 'Lỗi');
        return;
    }
    
    console.log('📋 Table data:', table);
    
    // Get current table data from the table row
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) {
        window.notificationManager?.show('Không tìm thấy bảng bàn', 'error', 'Lỗi');
        return;
    }
    
    const tableBody = tablesContainer.querySelector('.table tbody');
    if (!tableBody) {
        window.notificationManager?.show('Không tìm thấy dữ liệu bàn', 'error', 'Lỗi');
        return;
    }
    
    const rows = tableBody.querySelectorAll('tr');
    let tableRow = null;
    
    for (let row of rows) {
        const editButton = row.querySelector('button[onclick*="editTable"]');
        if (editButton) {
            const onclickAttr = editButton.getAttribute('onclick');
            const tableIdMatch = onclickAttr.match(/editTable\('([^']+)'\)/);
            if (tableIdMatch && tableIdMatch[1] === tableId) {
                tableRow = row;
                break;
            }
        }
    }
    
    if (!tableRow) {
        window.notificationManager?.show('Không tìm thấy hàng bàn', 'error', 'Lỗi');
        return;
    }
    
    // Extract data from the row
    const tableNumberCell = tableRow.querySelector('td:first-child');
    const tableNameCell = tableRow.querySelector('td:nth-child(2)');
    const roomNameCell = tableRow.querySelector('td:nth-child(3)');
    const capacityCell = tableRow.querySelector('td:nth-child(4)');
    
    if (!tableNumberCell || !tableNameCell || !capacityCell) {
        window.notificationManager?.show('Dữ liệu bàn không đầy đủ', 'error', 'Lỗi');
        return;
    }
    
    const tableNumber = tableNumberCell.textContent.trim();
    const tableName = tableNameCell.textContent.trim();
    
    // Extract room name from badge
    let roomName = '';
    let roomId = null;
    if (roomNameCell) {
        const roomBadge = roomNameCell.querySelector('.room-badge');
        if (roomBadge) {
            roomName = roomBadge.textContent.trim();
            roomId = getRoomIdByName(roomName);
        } else {
            // Handle case where table has no room assigned
            roomName = '';
            roomId = null;
        }
    }
    
    const capacityText = capacityCell.textContent.trim();
    
    // Extract capacity number from text
    const capacityMatch = capacityText.match(/(\d+)/);
    const capacity = capacityMatch ? parseInt(capacityMatch[1]) : 0;
    
    console.log('📊 Extracted data:', {
        tableNumber: tableNumber,
        tableName: tableName,
        roomName: roomName,
        roomId: roomId,
        capacity: capacity
    });
    
    // Show edit modal
    showEditTableModal(tableId, tableNumber, tableName, roomId, capacity);
};

// Function to show edit table modal with pre-filled data
function showEditTableModal(tableId, tableNumber, tableName, roomId, capacity) {
    console.log('🔄 Showing edit table modal for:', tableId);
    
    // Get the add table modal
    const modal = document.getElementById('addTableModal');
    if (!modal) {
        window.notificationManager?.show('Không tìm thấy modal chỉnh sửa', 'error', 'Lỗi');
        return;
    }
    
    // Update modal title
    const modalTitle = modal.querySelector('.modal-header h2');
    if (modalTitle) {
        modalTitle.textContent = 'Chỉnh sửa bàn';
    }
    
    // Pre-fill form fields
    const tableNumberInput = document.getElementById('tableNumber');
    const tableNameInput = document.getElementById('tableName');
    const capacityInput = document.getElementById('capacity');
    const roomIdInput = document.getElementById('roomId');
    
    if (tableNumberInput) tableNumberInput.value = tableNumber;
    if (tableNameInput) tableNameInput.value = tableName;
    if (capacityInput) capacityInput.value = capacity;
    if (roomIdInput && roomId) roomIdInput.value = roomId;
    
    // Update submit button
    const submitButton = modal.querySelector('.btn-success');
    if (submitButton) {
        submitButton.textContent = '✅ Cập nhật';
        submitButton.onclick = function(event) {
            submitEditTable(event, tableId);
        };
    }
    
    // Show modal
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
    
    // Let CSS handle the height, don't override
    const modalContent = modal.querySelector('.modal-content');
    if (modalContent) {
        modalContent.style.height = '';
        modalContent.style.maxHeight = '';
    }
    
    // Update room limits if room is selected
    if (roomId) {
        updateRoomLimitsForEdit(tableId, roomId, capacity);
    }
    
    // Add event listener for room change in edit mode
    if (roomIdInput) {
        roomIdInput.addEventListener('change', function() {
            const selectedRoomId = this.value;
            if (selectedRoomId) {
                updateRoomLimitsForEdit(tableId, selectedRoomId, capacity);
            }
        });
    }
    
    // Focus on first input
    if (tableNumberInput) tableNumberInput.focus();
}

// Function to submit edit table
window.submitEditTable = async function(event, tableId) {
    if (event) {
        event.preventDefault();
    }
    
    console.log('🔄 Submitting edit table:', tableId);
    
    const form = document.getElementById('addTableForm');
    if (!form) {
        console.error('addTableForm not found');
        return;
    }
    
    // Get form values
    const tableNumberInput = document.getElementById('tableNumber');
    const tableNameInput = document.getElementById('tableName');
    const capacityInput = document.getElementById('capacity');
    const roomIdInput = document.getElementById('roomId');
    
    const tableNumber = tableNumberInput ? tableNumberInput.value : '';
    const tableName = tableNameInput ? tableNameInput.value : '';
    const capacity = capacityInput ? capacityInput.value : '';
    const roomId = roomIdInput ? roomIdInput.value : '';

    if (!tableNumber || tableNumber.trim() === '') {
        window.notificationManager?.show('Vui lòng nhập số bàn', 'warning', 'Thiếu thông tin');
        if (tableNumberInput) tableNumberInput.focus();
        return;
    }

    if (!tableName || tableName.trim() === '') {
        window.notificationManager?.show('Vui lòng nhập tên bàn', 'warning', 'Thiếu thông tin');
        if (tableNameInput) tableNameInput.focus();
        return;
    }

    if (!capacity || capacity.trim() === '') {
        window.notificationManager?.show('Vui lòng nhập sức chứa', 'warning', 'Thiếu thông tin');
        if (capacityInput) capacityInput.focus();
        return;
    }

    if (tableNumber.trim().length > 50) {
        window.notificationManager?.show('Số bàn không được vượt quá 50 ký tự', 'error', 'Lỗi nhập liệu');
        if (tableNumberInput) tableNumberInput.focus();
        return;
    }

    if (tableName.trim().length > 100) {
        window.notificationManager?.show('Tên bàn không được vượt quá 100 ký tự', 'error', 'Lỗi nhập liệu');
        if (tableNameInput) tableNameInput.focus();
        return;
    }

    const capacityNum = parseInt(capacity.trim());
    if (isNaN(capacityNum) || capacityNum < 1 || capacityNum > 20) {
        window.notificationManager?.show('Sức chứa phải từ 1 đến 20 người', 'error', 'Lỗi nhập liệu');
        if (capacityInput) capacityInput.focus();
        return;
    }

    // Check for duplicate table name and number in the same room (excluding current table being edited)
    if (roomId && roomId.trim() !== '') {
        if (isTableNameDuplicateInRoomForEdit(tableName.trim(), roomId, tableId)) {
            window.notificationManager?.show('Tên bàn đã tồn tại trong phòng này. Vui lòng chọn tên khác', 'error', 'Tên bàn trùng lặp');
            if (tableNameInput) {
                tableNameInput.focus();
                tableNameInput.select();
            }
            return;
        }
        
        if (isTableNumberDuplicateInRoomForEdit(tableNumber.trim(), roomId, tableId)) {
            window.notificationManager?.show('Số bàn đã tồn tại trong phòng này. Vui lòng chọn số khác', 'error', 'Số bàn trùng lặp');
            if (tableNumberInput) {
                tableNumberInput.focus();
                tableNumberInput.select();
            }
                    return;
                }
            }
            
    // Validate room limits if room is selected
    if (roomId && roomId.trim() !== '') {
        if (!validateCapacityAgainstRoomLimits(tableId, 'roomId', 'capacity')) {
                    return;
        }
    }

    // Show loading notification
    const loadingId = window.notificationManager?.show('Đang cập nhật bàn...', 'info', 'Đang xử lý', 0);
    
    // Add loading class to form
    form.classList.add('loading');
    
    try {
        // Sanitize strings to prevent JSON parsing errors
        const sanitizeString = (str) => {
            if (!str) return '';
            return str.trim()
                .replace(/[\u0000-\u001F\u007F-\u009F]/g, '') // Remove control characters
                .replace(/[\u2000-\u200F\u2028-\u202F\u205F-\u206F]/g, ' ') // Replace various spaces with regular space
                .replace(/\s+/g, ' ') // Replace multiple spaces with single space
                .trim();
        };
        
        // Create JSON payload with sanitized data
        const payload = {
            action: 'editTable',
            tableId: tableId,
            tableNumber: sanitizeString(tableNumber),
            tableName: sanitizeString(tableName),
            capacity: sanitizeString(capacity)
        };
        
        if (roomId && roomId.trim() !== '') {
            payload.roomId = sanitizeString(roomId);
        }
        
        // Debug: Log payload
        console.log('JSON payload:', payload);
        
        // Submit via AJAX with JSON
        const response = await fetch('roomtable', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(payload)
        });
        
        console.log('Response status:', response.status);
        
        if (response.ok) {
            const contentType = response.headers.get('content-type');
            console.log('Content-Type:', contentType);
            
            if (contentType && contentType.includes('application/json')) {
                try {
                    const responseText = await response.text();
                    console.log('Response text:', responseText);
                    
                    // Try to parse JSON with error handling
                    let result;
                    try {
                        result = JSON.parse(responseText);
                    } catch (parseError) {
                        console.error('JSON parse error:', parseError);
                        console.error('Raw response:', responseText);
                        
                        // Try to extract error message from raw text
                        const errorMatch = responseText.match(/"message"\s*:\s*"([^"]*)"/);
                        const errorMessage = errorMatch ? errorMatch[1] : 'Lỗi không xác định';
                        
                        result = {
                            success: false,
                            message: errorMessage
                        };
                    }
                    
                    console.log('Response JSON:', result);
                    
                    // Remove loading notification
                    if (loadingId) window.notificationManager?.remove(loadingId);
                    
                    if (result.success) {
                        // Show success notification
                        window.notificationManager?.show(result.message, 'success', 'Hoàn thành');
                        
                        // Reset form
                        form.reset();
                        
                        // Close modal
                        window.closeAddTableModal();
                        
                        // Update table (add or edit) without reloading
                        console.log('🔄 Updating table list...');
                        
                        // Get room name for display
                        let roomName = '';
                        if (roomId && roomId.trim() !== '') {
                            const selectedRoom = getRoomById(roomId);
                            if (selectedRoom) {
                                roomName = selectedRoom.name;
                            }
                        }
                        
                        // Get status from form or default to Available
                        const statusInput = document.getElementById('status');
                        const status = statusInput ? statusInput.value : 'Available';
                        
                        // Check if this is add or edit mode based on tableId existence
                        if (typeof tableId !== 'undefined' && tableId) {
                            // Edit mode: update existing table
                            updateTableRowInPlace(
                                tableId,
                                tableNumber.trim(), 
                                tableName.trim(), 
                                roomName, 
                                capacityNum, 
                                status
                            );
                        } else {
                            // Add mode: add new table to list with real tableId from server
                            addNewTableToList(
                                tableNumber.trim(), 
                                tableName.trim(), 
                                roomName, 
                                capacityNum, 
                                status,
                                result.tableId // Use real tableId from server
                            );
                            
                            // Update table count
                            updateTableCount();
                            
                            // Update room limits
                            window.updateRoomLimits();
                        }
                    } else {
                        // Show error notification
                        window.notificationManager?.show(result.message, 'error', 'Lỗi');
                        
                        // Don't close modal on error - let user fix the issue
                        // But ensure form is not in loading state
                        form.classList.remove('loading');
                    }
                } catch (error) {
                    console.error('Error processing response:', error);
                    
                    // Remove loading notification
                    if (loadingId) window.notificationManager?.remove(loadingId);
                    
                    // Show error notification
                    window.notificationManager?.show('Có lỗi xảy ra khi xử lý phản hồi từ server', 'error', 'Lỗi');
                    
                    // Ensure form is not in loading state
                    form.classList.remove('loading');
                }
            } else {
                // Try to get response as text to see what we're getting
                const responseText = await response.text();
                console.log('Response text:', responseText);
                throw new Error('Response is not JSON: ' + responseText.substring(0, 200));
            }
        } else {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
    } catch (error) {
        console.error('Error editing table:', error);
        
        // Remove loading notification
        if (loadingId) window.notificationManager?.remove(loadingId);
        
        // Show error notification
        window.notificationManager?.show('Có lỗi xảy ra khi cập nhật bàn', 'error', 'Lỗi');
        
        // Ensure form is not in loading state
        form.classList.remove('loading');
    } finally {
        // Remove loading class
        form.classList.remove('loading');
    }
};

window.deleteTable = function(tableId, event) {
    // Prevent any default behavior
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }
    
    console.log('🗑️ Delete table requested:', tableId);
    console.log('TableId type:', typeof tableId);
    console.log('TableId length:', tableId ? tableId.length : 'null');
    
    // Find the table name from the table
    let tablesTableBody = document.querySelectorAll('.room-table-container')[1];
    if (tablesTableBody) {
        tablesTableBody = tablesTableBody.querySelector('.table tbody');
    }
    
    console.log('Tables table body:', tablesTableBody);
    if (!tablesTableBody) {
        console.error('Tables table body not found');
        return;
    }
    
    // Find the row with the matching table ID
    const targetRow = tablesTableBody.querySelector(`tr[data-table-id="${tableId}"]`);
    let tableName = 'Unknown Table';
    let tableNumber = 'Unknown';
    
    console.log('Looking for table with ID:', tableId);
    console.log('Target row found:', targetRow);
    
    if (targetRow) {
        // Extract table name and number from the cells
        const numberCell = targetRow.querySelector('td:first-child');
        const nameCell = targetRow.querySelector('td:nth-child(2)');
        
        if (numberCell) {
            tableNumber = numberCell.textContent.trim();
        }
        if (nameCell) {
            tableName = nameCell.textContent.trim();
        }
        console.log('Found table info:', { tableNumber, tableName });
    } else {
        console.warn('Could not find row with data-table-id:', tableId);
        // Fallback: try the old method
        const rows = tablesTableBody.querySelectorAll('tr');
        console.log('Total rows found:', rows.length);
        
        for (let row of rows) {
            const deleteButton = row.querySelector('button[onclick*="deleteTable"]');
            if (deleteButton) {
                const onclickAttr = deleteButton.getAttribute('onclick');
                if (onclickAttr && onclickAttr.includes(tableId)) {
                    const numberCell = row.querySelector('td:first-child');
                    const nameCell = row.querySelector('td:nth-child(2)');
                    
                    if (numberCell) {
                        tableNumber = numberCell.textContent.trim();
                    }
                    if (nameCell) {
                        tableName = nameCell.textContent.trim();
                    }
                    console.log('Found table info (fallback):', { tableNumber, tableName });
                    break;
                }
            }
        }
    }
    
    // Store table ID for later use
    tableToDelete = tableId;
    
    // Update modal content
    const tableNameElement = document.getElementById('tableNameToDelete');
    console.log('Table name element:', tableNameElement);
    if (tableNameElement) {
        tableNameElement.textContent = `${tableNumber} - ${tableName}`;
        console.log('Updated table name in modal:', tableNameElement.textContent);
    } else {
        console.error('Table name element not found!');
    }
    
    // Show modal
    const modal = document.getElementById('deleteTableConfirmModal');
    console.log('Modal element:', modal);
    if (modal) {
        console.log('Showing delete confirmation modal');
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
        
        // Focus on cancel button for accessibility
        const cancelBtn = modal.querySelector('.btn-secondary');
        console.log('Cancel button:', cancelBtn);
        if (cancelBtn) {
            cancelBtn.focus();
        }
    } else {
        console.error('Delete confirmation modal not found!');
        alert('Không tìm thấy modal xác nhận xóa. Vui lòng tải lại trang.');
    }
};

window.changeTableStatus = async function(tableId, currentStatus) {
    const newStatus = prompt('Nhập trạng thái mới (Available/Occupied/Reserved):', currentStatus);
    if (!newStatus || newStatus === currentStatus) {
        return;
    }
    
    // Show loading notification
    const loadingId = window.notificationManager?.show('Đang cập nhật trạng thái...', 'info', 'Đang xử lý', 0);
    
    try {
        const formData = new FormData();
        formData.append('action', 'updateTableStatus');
        formData.append('tableId', tableId);
        formData.append('status', newStatus);
        
        const response = await fetch('roomtable', {
            method: 'POST',
            body: formData
        });
        
        if (response.ok) {
            // Remove loading notification
            if (loadingId) window.notificationManager?.remove(loadingId);
            
            // Show success notification
            window.notificationManager?.show('Cập nhật trạng thái thành công!', 'success', 'Hoàn thành');
            
            // Refresh data without page reload
            await window.roomTableManager?.refreshData();
            
        } else {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
    } catch (error) {
        console.error('Error updating table status:', error);
        
        // Remove loading notification
        if (loadingId) window.notificationManager?.remove(loadingId);
        
        // Show error notification
        window.notificationManager?.show('Có lỗi xảy ra khi cập nhật trạng thái', 'error', 'Lỗi');
    }
};

// Function to close delete table confirmation modal
window.closeDeleteTableConfirmModal = function() {
    const modal = document.getElementById('deleteTableConfirmModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        tableToDelete = null;
    }
};

// Function to show table history modal
window.showTableHistoryModal = function(tableId, tableNumber, tableName, roomName) {
    console.log('📊 Showing table history modal for:', { tableId, tableNumber, tableName, roomName });
    
    // Update modal title
    const modalTitle = document.querySelector('#tableHistoryModal .modal-header h2');
    if (modalTitle) {
        modalTitle.textContent = `Lịch sử giao dịch - ${tableNumber} (${tableName})`;
    }
    
    // Show loading
    const historyContent = document.getElementById('tableHistoryContent');
    if (historyContent) {
        historyContent.innerHTML = '<div class="loading-spinner"></div>';
    }
    
    // Show modal
    const modal = document.getElementById('tableHistoryModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    }
    
    // Load table history
    loadTableHistory(tableId, tableNumber, tableName, roomName);
};

// Function to load table history
function loadTableHistory(tableId, tableNumber, tableName, roomName) {
    console.log('🔄 Loading table history for table:', tableId);
    
    fetch('/LiteFlow/roomtable', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            action: 'getTableHistory',
            tableId: tableId
        })
    })
    .then(response => response.json())
    .then(data => {
        console.log('📊 Table history response:', data);
        
        if (data.success) {
            displayTableHistory(data);
        } else {
            showTableHistoryError(data.message || 'Không thể tải lịch sử');
        }
    })
    .catch(error => {
        console.error('❌ Error loading table history:', error);
        showTableHistoryError('Lỗi kết nối: ' + error.message);
    });
}

// Global variables for history polling
let historyPollingInterval = null;
let currentHistoryTableId = null;

// Function to display table history (NEW FORMAT - Invoice Timeline)
function displayTableHistory(data) {
    const historyContent = document.getElementById('tableHistoryContent');
    if (!historyContent) return;
    
    const { tableInfo, invoices } = data;
    
    // Check for empty invoices
    if (!invoices || invoices.length === 0) {
        historyContent.innerHTML = `
            <div class="notification-empty">
                <i class='bx bx-receipt'></i>
                <p>Chưa có hóa đơn thanh toán</p>
            </div>
        `;
        // Update stats to 0
        document.getElementById('historyTotalInvoices').textContent = '0';
        document.getElementById('historyTotalRevenue').textContent = '0đ';
        return;
    }
    
    // Update stats
    document.getElementById('historyTotalInvoices').textContent = invoices.length;
    const totalRevenue = invoices.reduce((sum, inv) => sum + inv.finalAmount, 0);
    document.getElementById('historyTotalRevenue').textContent = totalRevenue.toLocaleString('vi-VN') + 'đ';
    
    // Group by date
    const grouped = {};
    invoices.forEach(invoice => {
        const timestamp = new Date(invoice.processedAt.replace(' ', 'T'));
        const date = timestamp.toLocaleDateString('vi-VN');
        const time = timestamp.toLocaleTimeString('vi-VN', {hour: '2-digit', minute: '2-digit'});
        
        if (!grouped[date]) grouped[date] = [];
        grouped[date].push({...invoice, displayTime: time});
    });
    
    // Build HTML
    let html = '';
    Object.keys(grouped).forEach(date => {
        html += `<div class="invoice-date-group">
            <div class="invoice-date-header">${date}</div>`;
        
        grouped[date].forEach(invoice => {
            const paymentMethodText = invoice.paymentMethod === 'cash' ? 'Tiền mặt' : 
                                     invoice.paymentMethod === 'card' ? 'Thẻ' : 'Chuyển khoản';
            
            // Items list
            let itemsHtml = '';
            if (invoice.items && invoice.items.length > 0) {
                itemsHtml = '<div class="invoice-items-list">';
                invoice.items.forEach(item => {
                    const itemName = item.name + (item.size ? ' (' + item.size + ')' : '');
                    itemsHtml += `<div class="invoice-item-row">• ${itemName} x${item.quantity}</div>`;
                });
                itemsHtml += '</div>';
            }
            
            const voucherHtml = invoice.hasVoucher ? 
                `<div class="invoice-detail invoice-voucher">✨ Voucher: -${invoice.discount.toLocaleString('vi-VN')}đ</div>` : '';
            
            html += `
                <div class="invoice-item" data-id="${invoice.transactionId}">
                    <div class="invoice-icon">
                        <i class='bx bx-receipt'></i>
                    </div>
                    <div class="invoice-content">
                        <div class="invoice-title">${invoice.invoiceName || invoice.tableName}</div>
                        <div class="invoice-detail"><strong>Số món:</strong> ${invoice.itemCount}</div>
                        ${itemsHtml}
                        <div class="invoice-detail"><strong>Tổng tiền:</strong> ${invoice.amount.toLocaleString('vi-VN')}đ</div>
                        ${voucherHtml}
                        <div class="invoice-detail invoice-final"><strong>Thanh toán:</strong> ${invoice.finalAmount.toLocaleString('vi-VN')}đ (${paymentMethodText})</div>
                        <div class="invoice-detail"><strong>Nhân viên:</strong> ${invoice.processedByName}</div>
                        <div class="invoice-time">${invoice.displayTime}</div>
                    </div>
                </div>
            `;
        });
        
        html += '</div>';
    });
    
    historyContent.innerHTML = html;
}

// Function to show table history error
function showTableHistoryError(message) {
    const historyContent = document.getElementById('tableHistoryContent');
    if (historyContent) {
        historyContent.innerHTML = `
            <div class="error-message">
                <div class="error-icon">❌</div>
                <div class="error-text">${message}</div>
                <button class="retry-btn" onclick="loadTableHistory()">Thử lại</button>
            </div>
        `;
    }
}

// Helper functions for status text
function getSessionStatusText(status) {
    const statusMap = {
        'Active': 'Đang hoạt động',
        'Completed': 'Đã hoàn thành',
        'Cancelled': 'Đã hủy'
    };
    return statusMap[status] || status;
}

function getOrderStatusText(status) {
    const statusMap = {
        'Pending': 'Chờ xử lý',
        'Preparing': 'Đang chuẩn bị',
        'Ready': 'Sẵn sàng',
        'Served': 'Đã phục vụ',
        'Cancelled': 'Đã hủy'
    };
    return statusMap[status] || status;
}

function getPaymentMethodText(method) {
    const methodMap = {
        'Cash': 'Tiền mặt',
        'Card': 'Thẻ',
        'Transfer': 'Chuyển khoản',
        'Wallet': 'Ví điện tử'
    };
    return methodMap[method] || method;
}

function getPaymentStatusText(status) {
    const statusMap = {
        'Unpaid': 'Chưa thanh toán',
        'Paid': 'Đã thanh toán',
        'Partial': 'Thanh toán một phần',
        'Pending': 'Chờ xử lý',
        'Completed': 'Hoàn thành',
        'Failed': 'Thất bại',
        'Refunded': 'Đã hoàn tiền'
    };
    return statusMap[status] || status;
}

window.closeTableHistoryModal = function() {
    const modal = document.getElementById('tableHistoryModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
};

// Function to confirm and execute table deletion
window.confirmDeleteTable = async function() {
    if (!tableToDelete) {
        console.error('No table ID to delete');
        return;
    }
    
    console.log('🗑️ Confirming deletion of table:', tableToDelete);
    
    const modal = document.getElementById('deleteTableConfirmModal');
    const confirmBtn = document.getElementById('confirmDeleteTableBtn');
    
    if (!confirmBtn) {
        console.error('Confirm delete table button not found');
        return;
    }
    
    // Disable button and show loading state
    confirmBtn.disabled = true;
    confirmBtn.innerHTML = '⏳ Đang xóa...';
    
    // Show loading notification
    const loadingId = window.notificationManager?.show('Đang xóa bàn...', 'info', 'Đang xử lý', 0);
    
    try {
        // Send delete request
        const response = await fetch('roomtable', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                action: 'deleteTable',
                tableId: tableToDelete
            })
        });
        
        console.log('Delete table response status:', response.status);
        
        if (response.ok) {
            const contentType = response.headers.get('content-type');
            console.log('Content-Type:', contentType);
            
            if (contentType && contentType.includes('application/json')) {
                try {
                    const responseText = await response.text();
                    console.log('Response text:', responseText);
                    
                    let result;
                    try {
                        result = JSON.parse(responseText);
                    } catch (parseError) {
                        console.error('JSON Parse Error:', parseError);
                        const errorMatch = responseText.match(/"message":\s*"([^"]*?)"/);
                        const errorMessage = errorMatch ? errorMatch[1] : 'Lỗi không xác định từ server';
                        
                        if (loadingId) window.notificationManager?.remove(loadingId);
                        window.notificationManager?.show(errorMessage, 'error', 'Lỗi JSON');
                        return;
                    }
                    
                    console.log('Response JSON:', result);
                    
                    if (loadingId) window.notificationManager?.remove(loadingId);
                    
                    if (result.success) {
                        // Show success notification
                        window.notificationManager?.show(result.message, 'success', 'Hoàn thành');
                        
                        // Store tableToDelete before closing modal
                        const tableIdToDelete = tableToDelete;
                        console.log('🔍 Stored tableToDelete before closing modal:', tableIdToDelete);
                        
                        // Close modal
                        window.closeDeleteTableConfirmModal();
                        
                        // Remove table from DOM and shift rows up
                        removeTableFromList(tableIdToDelete);
                        
                        // Update room limits in dropdown
                        updateAllRoomOptions();
                        
                    } else {
                        // Show error notification
                        window.notificationManager?.show(result.message, 'error', 'Lỗi');
                        
                        // Close modal and reset button state
                        window.closeDeleteTableConfirmModal();
                    }
                } catch (error) {
                    console.error('Error processing delete response:', error);
                    
                    if (loadingId) window.notificationManager?.remove(loadingId);
                    window.notificationManager?.show('Có lỗi xảy ra khi xử lý phản hồi từ server', 'error', 'Lỗi');
                    
                    // Close modal and reset button state
                    window.closeDeleteTableConfirmModal();
                }
            } else {
                // Try to get response as text
                const responseText = await response.text();
                console.log('Response text:', responseText);
                throw new Error('Response is not JSON: ' + responseText.substring(0, 200));
            }
        } else {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
    } catch (error) {
        console.error('Error deleting table:', error);
        
        if (loadingId) window.notificationManager?.remove(loadingId);
        window.notificationManager?.show('Có lỗi xảy ra khi xóa bàn', 'error', 'Lỗi');
        
        // Close modal and reset button state
        window.closeDeleteTableConfirmModal();
    } finally {
        // Reset button state
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.innerHTML = '🗑️ Xóa';
        }
    }
};

// Function to remove table from DOM list
function removeTableFromList(tableId) {
    console.log('🗑️ Removing table from list:', tableId);
    
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) {
        console.error('Tables container not found');
        return;
    }
    
    const tableBody = tablesContainer.querySelector('.table tbody');
    if (!tableBody) {
        console.error('Tables table body not found');
        return;
    }
    
    // Find and remove the row using data attribute
    const rowToRemove = tableBody.querySelector(`tr[data-table-id="${tableId}"]`);
    
    if (rowToRemove) {
        console.log('✅ Found row with data-table-id:', tableId);
        
        // Remove row immediately without animation for debugging
        if (rowToRemove.parentNode) {
            rowToRemove.parentNode.removeChild(rowToRemove);
            console.log('✅ Table row removed from DOM (immediate)');
            
            // Update table count stats
            updateTableCount();
            
            // Check if this was the last table and show empty state
            const remainingRows = tableBody.querySelectorAll('tr[data-table-id]');
            if (remainingRows.length === 0) {
                console.log('🪑 No tables left, showing empty state');
                showTablesEmptyState();
            }
            
            // Smart pagination refresh after deletion
            console.log('🔄 Calling refreshPaginationAfterDeletion (immediate)...');
            refreshPaginationAfterDeletion();
        } else {
            console.warn('⚠️ Parent node not found for row to remove:', rowToRemove);
        }
    } else {
        console.warn('Table row not found for ID:', tableId);
    }
};

// Function to remove all tables belonging to a specific room
function removeTablesByRoomId(roomId) {
    console.log('🗑️ Removing all tables for room:', roomId);
    
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) {
        console.error('Tables container not found');
        return;
    }
    
    // Check if we're in empty state
    const tablesEmptyState = tablesContainer.querySelector('.empty-state');
    const isTablesEmptyStateVisible = tablesEmptyState && tablesEmptyState.style.display !== 'none';
    
    if (isTablesEmptyStateVisible) {
        console.log('🪑 Tables already in empty state, skipping removal');
        return;
    }
    
    const tableBody = tablesContainer.querySelector('.table tbody');
    if (!tableBody) {
        console.log('🪑 Tables table body not found, likely in empty state');
        return;
    }
    
    // Find all rows that belong to this room
    const rowsToRemove = tableBody.querySelectorAll(`tr[data-room-id="${roomId}"]`);
    
    console.log(`Found ${rowsToRemove.length} tables belonging to room ${roomId}`);
    
    // Remove each row
    rowsToRemove.forEach((row, index) => {
        console.log(`Removing table ${index + 1} for room ${roomId}`);
        
        if (row.parentNode) {
            row.parentNode.removeChild(row);
            console.log(`✅ Table row ${index + 1} removed from DOM`);
        }
    });
    
    // Update table count stats after removing all tables
    if (rowsToRemove.length > 0) {
        updateTableCount();
        console.log('🔄 Updated table count after removing room tables');
        
        // Check if no tables left and show empty state
        const remainingTableRows = tableBody.querySelectorAll('tr[data-table-id]');
        if (remainingTableRows.length === 0) {
            console.log('🪑 No tables left after removing room tables, showing empty state');
            showTablesEmptyState();
        }
    }
}

// Function to update data-room-id for existing table rows on page load
function updateTableRoomIds() {
    console.log('🔄 Updating data-room-id for existing table rows...');
    
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) {
        console.error('Tables container not found');
        return;
    }
    
    // Check if we're in empty state
    const tablesEmptyState = tablesContainer.querySelector('.empty-state');
    const isTablesEmptyStateVisible = tablesEmptyState && tablesEmptyState.style.display !== 'none';
    
    if (isTablesEmptyStateVisible) {
        console.log('🔄 Tables in empty state, skipping room ID update');
        return;
    }
    
    const tableBody = tablesContainer.querySelector('.table tbody');
    if (!tableBody) {
        console.log('🔄 Tables table body not found, likely in empty state');
        return;
    }
    
    const tableRows = tableBody.querySelectorAll('tr[data-table-id]');
    console.log(`Found ${tableRows.length} table rows to update`);
    
    tableRows.forEach((row, index) => {
        // Check if row already has data-room-id
        if (row.hasAttribute('data-room-id')) {
            console.log(`Row ${index + 1} already has data-room-id`);
            return;
        }
        
        // Get room name from the room badge
        const roomBadge = row.querySelector('.room-badge');
        if (roomBadge) {
            const roomName = roomBadge.textContent.trim();
            const roomId = getRoomIdByName(roomName);
            
            if (roomId) {
                row.setAttribute('data-room-id', roomId);
                console.log(`✅ Updated row ${index + 1} with room ID: ${roomId} for room: ${roomName}`);
            } else {
                console.log(`⚠️ Could not find room ID for room: ${roomName}`);
            }
        } else {
            console.log(`Row ${index + 1} has no room badge`);
        }
    });
}

window.searchItems = function() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const rows = document.querySelectorAll('.table tbody tr');

    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        if (text.includes(searchTerm)) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
};

window.sortTable = function(columnIndex, dataType, tableType) {
    console.log('🔄 Sorting table:', tableType, 'column:', columnIndex, 'type:', dataType);
    
    // Tìm bảng theo cách đơn giản hơn
    let table;
    if (tableType === 'rooms') {
        // Tìm bảng phòng đầu tiên
        table = document.querySelector('.room-table-container .table');
    } else {
        // Tìm bảng bàn thứ hai
        const containers = document.querySelectorAll('.room-table-container');
        table = containers.length > 1 ? containers[1].querySelector('.table') : null;
    }
    
    if (!table) {
        console.error('❌ Table not found for type:', tableType);
        return;
    }
    
    console.log('✅ Table found:', table);
    
    const tbody = table.querySelector('tbody');
    if (!tbody) {
        console.log('Tbody not found');
        return;
    }
    
    const rows = Array.from(tbody.querySelectorAll('tr'));
    console.log('Found rows:', rows.length);
    
    if (rows.length === 0) {
        console.log('No rows to sort');
        return;
    }
    
    // Xóa class sort cũ cho bảng hiện tại
    const currentTableHeaders = table.querySelectorAll('th');
    currentTableHeaders.forEach(th => {
        th.classList.remove('sort-asc', 'sort-desc');
    });

    // Xác định hướng sắp xếp
    console.log('Previous sort:', window.currentSortColumn, window.currentSortTable, window.currentSortDirection);
    if (window.currentSortColumn === columnIndex && window.currentSortTable === tableType) {
        window.currentSortDirection = window.currentSortDirection === 'asc' ? 'desc' : 'asc';
        console.log('🔄 Toggle sort direction to:', window.currentSortDirection);
    } else {
        window.currentSortDirection = 'asc';
        console.log('🆕 New sort direction:', window.currentSortDirection);
    }
    window.currentSortColumn = columnIndex;
    window.currentSortTable = tableType;

    // Thêm class sort cho header hiện tại
    const currentHeader = currentTableHeaders[columnIndex];
    if (currentHeader) {
        currentHeader.classList.add(window.currentSortDirection === 'asc' ? 'sort-asc' : 'sort-desc');
        console.log('✅ Added sort class:', window.currentSortDirection === 'asc' ? 'sort-asc' : 'sort-desc');
    }

    // Sắp xếp các hàng
    console.log('🔄 Starting sort with direction:', window.currentSortDirection);
    rows.sort((a, b) => {
        let aValue, bValue;
        
        try {
            if (tableType === 'rooms') {
                // Sắp xếp cho bảng phòng
                if (columnIndex === 0) { // Tên phòng
                    aValue = a.cells[0].textContent.trim();
                    bValue = b.cells[0].textContent.trim();
                } else if (columnIndex === 2) { // Ngày tạo
                    const aDateStr = a.cells[2].textContent.trim();
                    const bDateStr = b.cells[2].textContent.trim();
                    aValue = aDateStr === 'N/A' ? new Date(0) : new Date(aDateStr);
                    bValue = bDateStr === 'N/A' ? new Date(0) : new Date(bDateStr);
                } else if (columnIndex === 3) { // Số lượng bàn
                    const aText = a.cells[3].textContent.trim();
                    const bText = b.cells[3].textContent.trim();
                    // Extract number from "Tối đa X bàn" or "Chưa thiết lập"
                    const aMatch = aText.match(/Tối đa (\d+) bàn/);
                    const bMatch = bText.match(/Tối đa (\d+) bàn/);
                    aValue = aMatch ? parseInt(aMatch[1]) : 0;
                    bValue = bMatch ? parseInt(bMatch[1]) : 0;
                } else if (columnIndex === 4) { // Tổng sức chứa
                    const aText = a.cells[4].textContent.trim();
                    const bText = b.cells[4].textContent.trim();
                    // Extract number from "Tối đa X người" or "Chưa thiết lập"
                    const aMatch = aText.match(/Tối đa (\d+) người/);
                    const bMatch = bText.match(/Tối đa (\d+) người/);
                    aValue = aMatch ? parseInt(aMatch[1]) : 0;
                    bValue = bMatch ? parseInt(bMatch[1]) : 0;
                }
            } else {
                // Sắp xếp cho bảng bàn
                if (columnIndex === 0) { // Số bàn
                    const aText = a.cells[0].textContent.trim();
                    const bText = b.cells[0].textContent.trim();
                    // Try to parse as numbers first, fallback to string comparison
                    const aNum = parseInt(aText);
                    const bNum = parseInt(bText);
                    if (!isNaN(aNum) && !isNaN(bNum)) {
                        aValue = aNum;
                        bValue = bNum;
                    } else {
                        aValue = aText;
                        bValue = bText;
                    }
                } else if (columnIndex === 1) { // Tên bàn
                    aValue = a.cells[1].textContent.trim();
                    bValue = b.cells[1].textContent.trim();
                } else if (columnIndex === 2) { // Phòng
                    aValue = a.cells[2].textContent.trim();
                    bValue = b.cells[2].textContent.trim();
                } else if (columnIndex === 3) { // Sức chứa
                    const aText = a.cells[3].textContent.trim();
                    const bText = b.cells[3].textContent.trim();
                    // Extract number from "X người" format
                    const aMatch = aText.match(/(\d+)/);
                    const bMatch = bText.match(/(\d+)/);
                    aValue = aMatch ? parseInt(aMatch[1]) : 0;
                    bValue = bMatch ? parseInt(bMatch[1]) : 0;
                } else if (columnIndex === 4) { // Trạng thái
                    aValue = a.cells[4].textContent.trim();
                    bValue = b.cells[4].textContent.trim();
                } else if (columnIndex === 5) { // Ngày tạo
                    const aDateStr = a.cells[5].textContent.trim();
                    const bDateStr = b.cells[5].textContent.trim();
                    aValue = aDateStr === 'N/A' ? new Date(0) : new Date(aDateStr);
                    bValue = bDateStr === 'N/A' ? new Date(0) : new Date(bDateStr);
                }
            }

            // So sánh dựa trên kiểu dữ liệu
            let comparison = 0;
            if (dataType === 'number') {
                // Xử lý trường hợp NaN
                if (isNaN(aValue)) aValue = 0;
                if (isNaN(bValue)) bValue = 0;
                comparison = aValue - bValue;
            } else if (dataType === 'date') {
                // Xử lý trường hợp Invalid Date
                if (isNaN(aValue.getTime())) aValue = new Date(0);
                if (isNaN(bValue.getTime())) bValue = new Date(0);
                comparison = aValue - bValue;
            } else {
                // Xử lý trường hợp null/undefined
                const aStr = aValue || '';
                const bStr = bValue || '';
                comparison = aStr.localeCompare(bStr, 'vi', { numeric: true });
            }

            const result = window.currentSortDirection === 'asc' ? comparison : -comparison;
            
            // Debug log cho lần đầu
            if (Math.random() < 0.1) { // Chỉ log 10% để không spam
                console.log('Sort comparison:', aValue, 'vs', bValue, '=', result);
            }
            
            return result;
        } catch (error) {
            console.error('Error sorting:', error);
            return 0;
        }
    });

    // Cập nhật DOM
    console.log('🔄 Updating DOM with sorted rows...');
    
    // Xóa tất cả rows hiện tại
    tbody.innerHTML = '';
    
    // Thêm lại rows đã sắp xếp
    rows.forEach(row => {
        tbody.appendChild(row);
    });
    
    console.log('✅ Sorting completed successfully');
    console.log('Rows after sort:', tbody.querySelectorAll('tr').length);
};

// Initialize global variables for sorting
window.currentSortColumn = -1;
window.currentSortDirection = 'asc';
window.currentSortTable = '';

window.viewTableDetails = function(tableId) {
    fetch('roomtable?action=getTableDetails&tableId=' + tableId)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            // Check if response is JSON
            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                throw new Error('Response is not JSON');
            }
            
            return response.json();
        })
        .then(data => {
            if (data.error) {
                alert('Lỗi: ' + data.error);
                return;
            }
            
            let details = `
                <h3>📋 Chi tiết bàn ${data.tableNumber}</h3>
                <div style="text-align: left; margin: 20px;">
                    <p><strong>Tên bàn:</strong> ${data.tableName}</p>
                    <p><strong>Sức chứa:</strong> ${data.capacity} người</p>
                    <p><strong>Phòng:</strong> ${data.room ? data.room.name : 'Không có phòng'}</p>
                    <p><strong>Trạng thái:</strong> ${data.status}</p>
                    <p><strong>Trạng thái hoạt động:</strong> ${data.isActive ? 'Hoạt động' : 'Ngừng hoạt động'}</p>
            `;
            
            if (data.activeSession) {
                details += `
                    <hr>
                    <h4>🔄 Phiên đang hoạt động:</h4>
                    <p><strong>Khách hàng:</strong> ${data.activeSession.customerName || 'Khách vãng lai'}</p>
                    <p><strong>SĐT:</strong> ${data.activeSession.customerPhone || 'Không có'}</p>
                    <p><strong>Thời gian vào:</strong> ${formatDate(data.activeSession.checkInTime)}</p>
                    <p><strong>Tổng tiền:</strong> ${formatNumber(data.activeSession.totalAmount)} VNĐ</p>
                    <p><strong>Trạng thái thanh toán:</strong> ${data.activeSession.paymentStatus}</p>
                `;
            } else {
                details += `<p><strong>Trạng thái:</strong> Bàn trống</p>`;
            }
            
            details += `</div>`;
            
            // Create modal
            showModal('Chi tiết bàn', details);
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Có lỗi xảy ra khi lấy thông tin bàn');
        });
};

// Polling management functions
function startHistoryPolling(tableId) {
    currentHistoryTableId = tableId;
    if (historyPollingInterval) clearInterval(historyPollingInterval);
    
    // Poll every 5 seconds when modal is open
    historyPollingInterval = setInterval(() => {
        if (document.getElementById('tableHistoryModal').style.display === 'block') {
            loadTableHistory(currentHistoryTableId);
        } else {
            stopHistoryPolling();
        }
    }, 5000);
}

function stopHistoryPolling() {
    if (historyPollingInterval) {
        clearInterval(historyPollingInterval);
        historyPollingInterval = null;
    }
    currentHistoryTableId = null;
}

function closeTableHistoryModal() {
    stopHistoryPolling();
    document.getElementById('tableHistoryModal').style.display = 'none';
    document.body.style.overflow = 'auto';
}

function refreshTableHistory() {
    if (currentHistoryTableId) {
        loadTableHistory(currentHistoryTableId);
    }
}

// Expose functions to global scope
window.closeTableHistoryModal = closeTableHistoryModal;
window.refreshTableHistory = refreshTableHistory;

window.viewTableHistory = function(tableId) {
    // Show modal
    const modal = document.getElementById('tableHistoryModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    }
    
    // Load history
    loadTableHistory(tableId);
    
    // Start polling
    startHistoryPolling(tableId);
};

// DEPRECATED: Old viewTableHistory code (keeping for backwards compatibility)
function viewTableHistoryOld(tableId) {
    fetch('roomtable?action=getTableHistory&tableId=' + tableId)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            // Check if response is JSON
            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                throw new Error('Response is not JSON');
            }
            
            return response.json();
        })
        .then(data => {
            if (data.error) {
                alert('Lỗi: ' + data.error);
                return;
            }
            
            let history = `
                <h3>📋 Lịch sử giao dịch bàn (OLD)</h3>
                <div style="max-height: 400px; overflow-y: auto;">
            `;
            
            if (data.sessions && data.sessions.length > 0) {
                history += `
                    <table class="table" style="margin-top: 20px;">
                        <thead>
                            <tr>
                                <th>Khách hàng</th>
                                <th>SĐT</th>
                                <th>Vào</th>
                                <th>Ra</th>
                                <th>Trạng thái</th>
                                <th>Tổng tiền</th>
                                <th>Thanh toán</th>
                            </tr>
                        </thead>
                        <tbody>
                `;
                
                data.sessions.forEach(session => {
                    history += `
                        <tr>
                            <td>${session.customerName || 'Khách vãng lai'}</td>
                            <td>${session.customerPhone || '-'}</td>
                            <td>${formatDate(session.checkInTime)}</td>
                            <td>${session.checkOutTime ? formatDate(session.checkOutTime) : 'Chưa ra'}</td>
                            <td><span class="status ${session.status.toLowerCase()}">${session.status}</span></td>
                            <td>${formatNumber(session.totalAmount)} VNĐ</td>
                            <td><span class="payment-status ${session.paymentStatus.toLowerCase()}">${session.paymentStatus}</span></td>
                        </tr>
                    `;
                });
                
                history += `</tbody></table>`;
            } else {
                history += `<p style="text-align: center; margin: 40px; color: #666;">Chưa có lịch sử giao dịch nào</p>`;
            }
            
            history += `</div>`;
            
            // Create modal
            showModal('Lịch sử giao dịch', history);
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Có lỗi xảy ra khi lấy lịch sử bàn');
        });
};

window.showModal = function(title, content) {
    // Remove existing modal if any
    const existingModal = document.getElementById('dynamicModal');
    if (existingModal) {
        existingModal.remove();
    }
    
    // Create modal
    const modal = document.createElement('div');
    modal.id = 'dynamicModal';
    modal.className = 'modal';
    modal.style.display = 'block';
    modal.innerHTML = `
        <div class="modal-content" style="max-width: 800px;">
            <div class="modal-header">
                <h2>${title}</h2>
                <span class="close" onclick="closeDynamicModal()">&times;</span>
            </div>
            <div class="modal-body">
                ${content}
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" onclick="closeDynamicModal()">
                    ✅ Đóng
                </button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
};

window.closeDynamicModal = function() {
    const modal = document.getElementById('dynamicModal');
    if (modal) {
        modal.remove();
    }
};

// Helper functions for safe formatting
window.formatDate = function(dateString) {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return 'N/A';
        
        // Format as dd/MM/yyyy HH:mm
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        
        return `${day}/${month}/${year} ${hours}:${minutes}`;
    } catch (error) {
        console.warn('Error formatting date:', error);
        return 'N/A';
    }
};

window.formatNumber = function(number) {
    if (!number || isNaN(number)) return '0';
    return Number(number).toLocaleString('vi-VN');
};

// Global variables for caching and performance
let tablesCache = new Map();
let roomsCache = new Map();
let lastUpdateTime = 0;
const CACHE_DURATION = 30000; // 30 seconds

// Performance monitoring
const performanceMonitor = {
    startTime: 0,
    endTime: 0,
    
    start: function() {
        this.startTime = performance.now();
    },
    
    end: function(operation) {
        this.endTime = performance.now();
        const duration = this.endTime - this.startTime;
        console.log(`⚡ ${operation} completed in ${duration.toFixed(2)}ms`);
        return duration;
    }
};

// Enhanced table management class with UI/UX enhancements
class RoomTableManager {
    constructor() {
        this.init();
        this.setupEventListeners();
        this.setupAnimations();
        this.setupPerformanceOptimizations();
        this.startAutoRefresh();
    }
    
    init() {
        console.log('🚀 Initializing Enhanced Room Table Manager');
        this.loadInitialData();
        this.setupKeyboardShortcuts();
        this.setupSearchFunctionality();
        this.setupTableSorting();
        this.setupStatusAnimations();
        this.setupModalEnhancements();
        this.setupRealTimeUpdates();
    }
    
    setupEventListeners() {
        // Debounced search
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', this.debounce(this.handleSearch.bind(this), 300));
        }
        
        // Table row click events
        document.addEventListener('click', this.handleTableClick.bind(this));
        
        // Modal close events
        window.addEventListener('click', this.handleModalClose.bind(this));
        
        // Keyboard navigation
        document.addEventListener('keydown', this.handleKeyboardNavigation.bind(this));

        // Button interactions
        document.querySelectorAll('.btn').forEach(btn => {
            btn.addEventListener('mouseenter', this.handleButtonHover.bind(this));
            btn.addEventListener('mouseleave', this.handleButtonLeave.bind(this));
            btn.addEventListener('click', this.handleButtonClick.bind(this));
        });

        // Table row interactions
        document.querySelectorAll('.table tr').forEach(row => {
            row.addEventListener('mouseenter', this.handleRowHover.bind(this));
            row.addEventListener('mouseleave', this.handleRowLeave.bind(this));
        });

        // Status badges
        document.querySelectorAll('.status').forEach(status => {
            status.addEventListener('mouseenter', this.handleStatusHover.bind(this));
            status.addEventListener('mouseleave', this.handleStatusLeave.bind(this));
        });
    }
    
    setupAnimations() {
        // Staggered animations for stat cards
        const statCards = document.querySelectorAll('.stat-card');
        statCards.forEach((card, index) => {
            card.style.animationDelay = `${index * 0.1}s`;
            card.classList.add('animate-fade-in-up');
        });

        // Animate table rows on load - only for visible rows
        const tableRows = document.querySelectorAll('.table tbody tr:not(.pagination-hidden)');
        tableRows.forEach((row, index) => {
            row.style.animationDelay = `${index * 0.05}s`;
            row.classList.add('animate-fade-in-right');
        });

        // Animate section titles
        const sectionTitles = document.querySelectorAll('.section-title');
        sectionTitles.forEach(title => {
            title.classList.add('animate-fade-in-left');
        });

        // Animate toolbar
        const toolbar = document.querySelector('.toolbar');
        if (toolbar) {
            toolbar.classList.add('animate-fade-in-down');
        }
    }
    
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
    
    throttle(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }
    
    async loadInitialData() {
        performanceMonitor.start();
        
        try {
            // Load data with caching
            await this.loadTablesWithCache();
            await this.loadRoomsWithCache();
            
            performanceMonitor.end('Initial data load');
        } catch (error) {
            console.error('❌ Error loading initial data:', error);
            this.showNotification('Lỗi khi tải dữ liệu', 'error');
        }
    }
    
    async loadTablesWithCache() {
        console.log('🔄 loadTablesWithCache: Started');
        const now = Date.now();
        if (tablesCache.has('data') && (now - lastUpdateTime) < CACHE_DURATION) {
            console.log('🔄 loadTablesWithCache: Using cached data');
            return tablesCache.get('data');
        }
        
        try {
            console.log('🔄 loadTablesWithCache: Fetching fresh data...');
        // Fetch fresh data
        const response = await fetch('roomtable', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                action: 'getAllTables'
            })
        });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            // Check if response is JSON
            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                console.warn('Response is not JSON, skipping cache update');
                return [];
            }
            
        const data = await response.json();
            console.log('🔄 loadTablesWithCache: Data received, count:', data.length);
        
        tablesCache.set('data', data);
        lastUpdateTime = now;
            console.log('🔄 loadTablesWithCache: Data cached successfully');
        
        return data;
        } catch (error) {
            console.error('❌ Error loading tables:', error);
            return [];
        }
    }
    
    async loadRoomsWithCache() {
        console.log('🔄 loadRoomsWithCache: Started');
        const now = Date.now();
        if (roomsCache.has('data') && (now - lastUpdateTime) < CACHE_DURATION) {
            console.log('🔄 loadRoomsWithCache: Using cached data');
            return roomsCache.get('data');
        }
        
        try {
            console.log('🔄 loadRoomsWithCache: Fetching fresh data...');
        // Fetch fresh data
        const response = await fetch('roomtable', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                action: 'getAllRooms'
            })
        });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            // Check if response is JSON
            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                console.warn('Response is not JSON, skipping cache update');
                return [];
            }
            
        const data = await response.json();
            console.log('🔄 loadRoomsWithCache: Data received, count:', data.length);
        
        roomsCache.set('data', data);
            console.log('🔄 loadRoomsWithCache: Data cached successfully');
        return data;
        } catch (error) {
            console.error('❌ Error loading rooms:', error);
            return [];
        }
    }
    
    setupSearchFunctionality() {
        // Enhanced search with highlighting
        this.searchResults = [];
        this.currentSearchTerm = '';
    }
    
    handleSearch(event) {
        const searchTerm = event.target.value.toLowerCase();
        this.currentSearchTerm = searchTerm;
        
        if (searchTerm.length < 2) {
            this.clearSearchHighlights();
            return;
        }

        this.highlightSearchResults(searchTerm);
        this.animateSearchResults();
    }

    highlightSearchResults(searchTerm) {
        const tableRows = document.querySelectorAll('.table tbody tr');
        
        tableRows.forEach(row => {
            const text = row.textContent.toLowerCase();
            const isMatch = text.includes(searchTerm);
            
            if (isMatch) {
                row.classList.add('search-highlight');
                row.style.animation = 'pulse 0.6s ease-out';
            } else {
                row.classList.remove('search-highlight');
                row.style.opacity = '0.5';
            }
        });
    }

    clearSearchHighlights() {
        const tableRows = document.querySelectorAll('.table tbody tr');
        tableRows.forEach(row => {
            row.classList.remove('search-highlight');
            row.style.opacity = '1';
            row.style.animation = '';
        });
    }

    animateSearchResults() {
        const highlightedRows = document.querySelectorAll('.search-highlight:not(.pagination-hidden)');
        highlightedRows.forEach((row, index) => {
            row.style.animationDelay = `${index * 0.1}s`;
            row.classList.add('animate-scale-in');
        });
    }
    
    handleTableClick(event) {
        const target = event.target;
        
        // Handle action buttons
        if (target.classList.contains('btn')) {
            const action = target.getAttribute('onclick');
            if (action) {
                // Extract parameters from onclick
                const match = action.match(/\(['"]([^'"]+)['"]?\)/);
                if (match) {
                    const id = match[1];
                    this.handleTableAction(target, id);
                }
            }
        }
        
        // Handle row selection
        if (target.closest('tr')) {
            this.handleRowSelection(target.closest('tr'));
        }
    }
    
    handleTableAction(button, id) {
        const actionType = button.textContent.trim();
        
        switch (actionType) {
            case '👁️ Chi tiết':
                this.showTableDetails(id);
                break;
            case '✏️ Sửa':
                this.showEditTableModal(id);
                break;
            case '🔄 Đổi trạng thái':
                this.showStatusChangeModal(id);
                break;
            case '📋 Lịch sử':
                this.showTableHistory(id);
                break;
            case '🗑️ Xóa':
                this.confirmDeleteTable(id);
                break;
        }
    }
    
    handleRowSelection(row) {
        // Remove previous selection
        document.querySelectorAll('.table tbody tr').forEach(r => {
            r.classList.remove('selected');
        });
        
        // Add selection to current row
        row.classList.add('selected');
        
        // Store selected table ID
        const tableId = row.dataset.tableId;
        if (tableId) {
            this.selectedTableId = tableId;
        }
    }
    
    setupTableSorting() {
        // Enhanced table sorting with animations
        const sortableHeaders = document.querySelectorAll('.sortable');
        sortableHeaders.forEach(header => {
            header.addEventListener('click', this.handleSort.bind(this));
        });
    }

    handleSort(event) {
        const header = event.currentTarget;
        const table = header.closest('table');
        const tbody = table.querySelector('tbody');
        const rows = Array.from(tbody.querySelectorAll('tr'));
        
        // Add sorting animation
        tbody.style.transition = 'all 0.3s ease-out';
        tbody.style.opacity = '0.7';
        
        setTimeout(() => {
            // Sort logic would go here
            this.animateSortedRows(rows);
            tbody.style.opacity = '1';
        }, 150);
    }

    animateSortedRows(rows) {
        // Only animate visible rows (not pagination-hidden)
        const visibleRows = Array.from(rows).filter(row => !row.classList.contains('pagination-hidden'));
        visibleRows.forEach((row, index) => {
            row.style.animationDelay = `${index * 0.05}s`;
            row.classList.add('animate-slide-in-up');
        });
    }
    
    async showTableDetails(tableId) {
        try {
            performanceMonitor.start();
            
            const response = await fetch(`roomtable?action=getTableDetails&tableId=${tableId}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            // Check if response is JSON
            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                throw new Error('Response is not JSON');
            }
            
            const data = await response.json();
            
            if (data.error) {
                this.showNotification('Lỗi: ' + data.error, 'error');
                return;
            }
            
            const details = this.formatTableDetails(data);
            this.showModal('Chi tiết bàn', details);
            
            performanceMonitor.end('Table details load');
        } catch (error) {
            console.error('❌ Error loading table details:', error);
            this.showNotification('Có lỗi xảy ra khi lấy thông tin bàn', 'error');
        }
    }
    
    formatTableDetails(data) {
        let details = `
            <div class="table-details">
                <div class="detail-section">
                    <h4>📋 Thông tin cơ bản</h4>
                    <div class="detail-grid">
                        <div class="detail-item">
                            <label>Số bàn:</label>
                            <span class="detail-value">${data.tableNumber}</span>
                        </div>
                        <div class="detail-item">
                            <label>Tên bàn:</label>
                            <span class="detail-value">${data.tableName}</span>
                        </div>
                        <div class="detail-item">
                            <label>Sức chứa:</label>
                            <span class="detail-value">${data.capacity} người</span>
                        </div>
                        <div class="detail-item">
                            <label>Phòng:</label>
                            <span class="detail-value">${data.room ? data.room.name : 'Không có phòng'}</span>
                        </div>
                        <div class="detail-item">
                            <label>Trạng thái:</label>
                            <span class="status ${data.status.toLowerCase()}">${this.getStatusText(data.status)}</span>
                        </div>
                        <div class="detail-item">
                            <label>Hoạt động:</label>
                            <span class="detail-value">${data.isActive ? '✅ Hoạt động' : '❌ Ngừng hoạt động'}</span>
                        </div>
                    </div>
                </div>
        `;
        
        if (data.activeSession) {
            details += `
                <div class="detail-section">
                    <h4>🔄 Phiên đang hoạt động</h4>
                    <div class="detail-grid">
                        <div class="detail-item">
                            <label>Khách hàng:</label>
                            <span class="detail-value">${data.activeSession.customerName || 'Khách vãng lai'}</span>
                        </div>
                        <div class="detail-item">
                            <label>SĐT:</label>
                            <span class="detail-value">${data.activeSession.customerPhone || 'Không có'}</span>
                        </div>
                        <div class="detail-item">
                            <label>Thời gian vào:</label>
                            <span class="detail-value">${this.formatDate(data.activeSession.checkInTime)}</span>
                        </div>
                        <div class="detail-item">
                            <label>Tổng tiền:</label>
                            <span class="detail-value amount">${this.formatNumber(data.activeSession.totalAmount)} VNĐ</span>
                        </div>
                        <div class="detail-item">
                            <label>Thanh toán:</label>
                            <span class="payment-status ${data.activeSession.paymentStatus.toLowerCase()}">${this.getPaymentStatusText(data.activeSession.paymentStatus)}</span>
                        </div>
                    </div>
                </div>
            `;
        } else {
            details += `
                <div class="detail-section">
                    <h4>📊 Trạng thái</h4>
                    <p class="empty-state">Bàn đang trống</p>
                </div>
            `;
        }
        
        details += `</div>`;
        return details;
    }
    
    async showTableHistory(tableId) {
        try {
            performanceMonitor.start();
            
            const response = await fetch(`roomtable?action=getTableHistory&tableId=${tableId}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            // Check if response is JSON
            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                throw new Error('Response is not JSON');
            }
            
            const data = await response.json();
            
            if (data.error) {
                this.showNotification('Lỗi: ' + data.error, 'error');
                return;
            }
            
            const history = this.formatTableHistory(data);
            this.showModal('Lịch sử giao dịch', history);
            
            performanceMonitor.end('Table history load');
        } catch (error) {
            console.error('❌ Error loading table history:', error);
            this.showNotification('Có lỗi xảy ra khi lấy lịch sử bàn', 'error');
        }
    }
    
    formatTableHistory(data) {
        let history = `
            <div class="table-history">
                <div class="history-stats">
                    <div class="stat-item">
                        <span class="stat-number">${data.sessions ? data.sessions.length : 0}</span>
                        <span class="stat-label">Tổng phiên</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-number">${data.sessions ? this.formatNumber(data.sessions.reduce((sum, s) => sum + Number(s.totalAmount || 0), 0)) : '0'}</span>
                        <span class="stat-label">Tổng doanh thu (VNĐ)</span>
                    </div>
                </div>
        `;
        
        if (data.sessions && data.sessions.length > 0) {
            history += `
                <div class="history-table-container">
                    <table class="history-table">
                        <thead>
                            <tr>
                                <th>Khách hàng</th>
                                <th>SĐT</th>
                                <th>Vào</th>
                                <th>Ra</th>
                                <th>Trạng thái</th>
                                <th>Tổng tiền</th>
                                <th>Thanh toán</th>
                            </tr>
                        </thead>
                        <tbody>
            `;
            
            data.sessions.forEach(session => {
                history += `
                    <tr>
                        <td>${session.customerName || 'Khách vãng lai'}</td>
                        <td>${session.customerPhone || '-'}</td>
                        <td>${this.formatDate(session.checkInTime)}</td>
                        <td>${session.checkOutTime ? this.formatDate(session.checkOutTime) : 'Chưa ra'}</td>
                        <td><span class="status ${session.status.toLowerCase()}">${this.getStatusText(session.status)}</span></td>
                        <td class="amount">${this.formatNumber(session.totalAmount)} VNĐ</td>
                        <td><span class="payment-status ${session.paymentStatus.toLowerCase()}">${this.getPaymentStatusText(session.paymentStatus)}</span></td>
                    </tr>
                `;
            });
            
            history += `</tbody></table></div>`;
        } else {
            history += `<div class="empty-state"><p>Chưa có lịch sử giao dịch nào</p></div>`;
        }
        
        history += `</div>`;
        return history;
    }
    
    showModal(title, content) {
        // Remove existing modal
        const existingModal = document.getElementById('dynamicModal');
        if (existingModal) {
            existingModal.remove();
        }
        
        // Create modal with enhanced styling
        const modal = document.createElement('div');
        modal.id = 'dynamicModal';
        modal.className = 'modal enhanced-modal';
        modal.innerHTML = `
            <div class="modal-content enhanced-modal-content">
                <div class="modal-header">
                    <h2>${title}</h2>
                    <span class="close" onclick="roomTableManager.closeModal()">&times;</span>
                </div>
                <div class="modal-body enhanced-modal-body">
                    ${content}
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" onclick="roomTableManager.closeModal()">
                        ✅ Đóng
                    </button>
                </div>
            </div>
        `;
        
        document.body.appendChild(modal);
        modal.style.display = 'block';
        
        // Add animation
        setTimeout(() => modal.classList.add('show'), 10);
    }
    
    closeModal() {
        const modal = document.getElementById('dynamicModal');
        if (modal) {
            modal.classList.remove('show');
            setTimeout(() => modal.remove(), 300);
        }
    }
    
    handleModalClose(event) {
        const modal = document.getElementById('dynamicModal');
        if (event.target === modal) {
            this.closeModal();
        }
    }
    
    setupKeyboardShortcuts() {
        document.addEventListener('keydown', (event) => {
            // ESC to close modal
            if (event.key === 'Escape') {
                this.closeModal();
            }
            
            // Ctrl+F to focus search
            if (event.ctrlKey && event.key === 'f') {
                event.preventDefault();
                const searchInput = document.getElementById('searchInput');
                if (searchInput) {
                    searchInput.focus();
                }
            }
        });
    }
    
    handleKeyboardNavigation(event) {
        const selectedRow = document.querySelector('.table tbody tr.selected');
        if (!selectedRow) return;
        
        const rows = Array.from(document.querySelectorAll('.table tbody tr:not([style*="display: none"])'));
        const currentIndex = rows.indexOf(selectedRow);
        
        switch (event.key) {
            case 'ArrowUp':
                event.preventDefault();
                if (currentIndex > 0) {
                    this.handleRowSelection(rows[currentIndex - 1]);
                }
                break;
            case 'ArrowDown':
                event.preventDefault();
                if (currentIndex < rows.length - 1) {
                    this.handleRowSelection(rows[currentIndex + 1]);
                }
                break;
            case 'Enter':
                event.preventDefault();
                if (this.selectedTableId) {
                    this.showTableDetails(this.selectedTableId);
                }
                break;
        }
    }
    
    startAutoRefresh() {
        // Auto refresh every 30 seconds
        setInterval(() => {
            if (document.visibilityState === 'visible') {
                this.refreshData();
            }
        }, 30000);
    }
    
    async refreshData() {
        try {
            console.log('🔄 refreshData: Started');
            // Clear cache
            tablesCache.clear();
            roomsCache.clear();
            console.log('🔄 refreshData: Caches cleared');
            
            // Show loading notification
            const loadingId = window.notificationManager?.show('Đang tải dữ liệu...', 'info', 'Đang xử lý', 0);
            console.log('🔄 refreshData: Notification shown, loadingId:', loadingId);
            
            // Reload data
            console.log('🔄 refreshData: Calling loadTablesWithCache...');
            await this.loadTablesWithCache();
            console.log('🔄 refreshData: loadTablesWithCache completed');
            
            console.log('🔄 refreshData: Calling loadRoomsWithCache...');
            await this.loadRoomsWithCache();
            console.log('🔄 refreshData: loadRoomsWithCache completed');
            
            // Remove loading notification
            if (loadingId) window.notificationManager?.remove(loadingId);
            console.log('🔄 refreshData: Notification removed');
            
            // Update UI with new data
            console.log('🔄 refreshData: Calling updateTableData...');
            await this.updateTableData();
            console.log('🔄 refreshData: updateTableData completed');
            
            // Rebuild room dropdown to avoid conflicts
            rebuildRoomDropdown();
            console.log('🔄 refreshData: Room dropdown rebuilt');
            
            // Update capacity badges with warning colors
            updateCapacityBadges();
            console.log('🔄 refreshData: Capacity badges updated');
            
            console.log('🔄 Data refreshed successfully');
            
        } catch (error) {
            console.error('❌ Error refreshing data:', error);
            window.notificationManager?.show('Có lỗi xảy ra khi tải dữ liệu', 'error', 'Lỗi');
        }
    }
    
    async updateTableData() {
        try {
            console.log('🔄 Updating table data...');
            
            // Fetch updated HTML content
            const response = await fetch('roomtable?action=getTableHTML');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const html = await response.text();
            console.log('📄 Fetched HTML length:', html.length);
            console.log('📄 HTML preview:', html.substring(0, 500) + '...');
            
            // Parse the response and update tables
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');
            
            // Debug: Check what we got from the server
            const allContainers = doc.querySelectorAll('.room-table-container');
            console.log('📄 Found containers in response:', allContainers.length);
            allContainers.forEach((container, index) => {
                const table = container.querySelector('.table');
                const rows = table ? table.querySelectorAll('tbody tr').length : 0;
                console.log(`📄 Container ${index}: ${rows} rows`);
            });
            
            // Update rooms table - match the structure from table-fragment.jsp
            const roomsContainer = doc.querySelector('.room-table-container');
            if (roomsContainer) {
                const roomsTable = roomsContainer.querySelector('.table');
                if (roomsTable) {
                    // Find the first room-table-container (rooms section) in current page
                    const currentRoomsContainers = document.querySelectorAll('.room-table-container');
                    const currentRoomsContainer = currentRoomsContainers[0]; // First container is rooms
                    const currentRoomsTable = currentRoomsContainer ? currentRoomsContainer.querySelector('.table') : null;
                    
                    if (currentRoomsTable) {
                        console.log('🔄 Updating rooms table...');
                        console.log('Current rooms table rows:', currentRoomsTable.querySelectorAll('tbody tr').length);
                        console.log('New rooms table rows:', roomsTable.querySelectorAll('tbody tr').length);
                        
                        // Use replaceWith instead of innerHTML for better DOM update
                        const tbody = currentRoomsTable.querySelector('tbody');
                        const newTbody = roomsTable.querySelector('tbody');
                        
                        if (tbody && newTbody) {
                            console.log('🔄 Replacing tbody content...');
                            console.log('  - Current tbody rows:', tbody.querySelectorAll('tr').length);
                            console.log('  - New tbody rows:', newTbody.querySelectorAll('tr').length);
                            
                            // Clone the new tbody to avoid DOM issues
                            const clonedTbody = newTbody.cloneNode(true);
                            tbody.replaceWith(clonedTbody);
                            
                            // Verify the replacement worked
                            setTimeout(() => {
                                const newTbodyCheck = currentRoomsTable.querySelector('tbody');
                                const newRowsCount = newTbodyCheck ? newTbodyCheck.querySelectorAll('tr').length : 0;
                                console.log('✅ Rooms table tbody replaced, new rows count:', newRowsCount);
                                
                                if (newRowsCount === 0) {
                                    console.warn('⚠️ Tbody replacement failed, forcing page reload...');
                                    window.location.reload();
                                }
                            }, 50);
                        } else {
                            console.log('🔄 Fallback: Using innerHTML...');
                            currentRoomsTable.innerHTML = roomsTable.innerHTML;
                            console.log('✅ Rooms table updated with innerHTML');
                        }
                        
                        console.log('✅ Rooms table updated');
                    } else {
                        console.warn('⚠️ Current rooms table not found');
                    }
                } else {
                    console.warn('⚠️ Rooms table not found in response');
                }
            } else {
                console.warn('⚠️ Rooms container not found in response');
            }
            
            // Update tables table - match the structure from table-fragment.jsp
            const tablesContainers = doc.querySelectorAll('.room-table-container');
            if (tablesContainers.length > 1) {
                const tablesContainer = tablesContainers[1]; // Second container is tables
                const tablesTable = tablesContainer.querySelector('.table');
                if (tablesTable) {
                    // Find the second room-table-container (tables section) in current page
                    const currentTablesContainers = document.querySelectorAll('.room-table-container');
                    const currentTablesContainer = currentTablesContainers[1]; // Second container is tables
                    const currentTablesTable = currentTablesContainer ? currentTablesContainer.querySelector('.table') : null;
                    
                    if (currentTablesTable) {
                        console.log('🔄 Updating tables table...');
                        console.log('Current tables table rows:', currentTablesTable.querySelectorAll('tbody tr').length);
                        console.log('New tables table rows:', tablesTable.querySelectorAll('tbody tr').length);
                        
                        // Use replaceWith instead of innerHTML for better DOM update
                        const tbody = currentTablesTable.querySelector('tbody');
                        const newTbody = tablesTable.querySelector('tbody');
                        
                        if (tbody && newTbody) {
                            console.log('🔄 Replacing tables tbody content...');
                            tbody.replaceWith(newTbody);
                            console.log('✅ Tables table tbody replaced');
                        } else {
                            console.log('🔄 Fallback: Using innerHTML for tables...');
                            currentTablesTable.innerHTML = tablesTable.innerHTML;
                            console.log('✅ Tables table updated with innerHTML');
                        }
                        
                        console.log('✅ Tables table updated');
                    } else {
                        console.warn('⚠️ Current tables table not found');
                    }
                } else {
                    console.warn('⚠️ Tables table not found in response');
                }
            } else {
                console.warn('⚠️ Tables container not found in response');
            }
            
            // Update statistics
            const statsCards = doc.querySelectorAll('.stat-card');
            const currentStatsCards = document.querySelectorAll('.stat-card');
            console.log('🔄 Updating stats cards...', statsCards.length, 'found');
            statsCards.forEach((card, index) => {
                if (currentStatsCards[index]) {
                    currentStatsCards[index].innerHTML = card.innerHTML;
                }
            });
            
            // Re-setup event listeners for new elements
            console.log('🔄 Re-setting up event listeners...');
            this.setupEventListeners();
            
            // Format all dates after updating
            console.log('🔄 Formatting dates...');
            formatAllDates();
            
            console.log('✅ Table data update completed');
            
            // Verify the update worked
            setTimeout(() => {
                const currentRoomsCount = document.querySelectorAll('.room-table-container')[0]?.querySelectorAll('tbody tr').length || 0;
                console.log('🔍 Verification: Current rooms count after update:', currentRoomsCount);
                
                // If no rooms found or count is too low, force reload
                if (currentRoomsCount === 0 || currentRoomsCount < 2) {
                    console.warn('⚠️ Insufficient rooms found after update, forcing page reload...');
                    window.location.reload();
                } else {
                    console.log('✅ Room count verification passed:', currentRoomsCount);
                }
            }, 100);
            
        } catch (error) {
            console.error('Error updating table data:', error);
            console.log('🔄 Falling back to page reload...');
            // Fallback: reload the page
            window.location.reload();
        }
    }
    
    showNotification(message, type = 'info', title = null, duration = 3000) {
        // Create notification stack if it doesn't exist
        let stack = document.getElementById('notification-stack');
        if (!stack) {
            stack = document.createElement('div');
            stack.id = 'notification-stack';
            stack.className = 'notification-stack';
            document.body.appendChild(stack);
        }

        // Create notification element
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        
        // Get icon based on type
        const icons = {
            success: '✅',
            error: '❌',
            warning: '⚠️',
            info: 'ℹ️'
        };
        
        const icon = icons[type] || icons.info;
        
        // Create notification content
        notification.innerHTML = `
            <div class="notification-icon">${icon}</div>
            <div class="notification-content">
                ${title ? `<div class="notification-title">${title}</div>` : ''}
                <div class="notification-message">${message}</div>
            </div>
            <div class="notification-close" onclick="this.parentElement.remove()">×</div>
            <div class="notification-progress"></div>
        `;
        
        // Add to stack
        stack.appendChild(notification);
        
        // Trigger animation
        requestAnimationFrame(() => {
            notification.classList.add('show');
        });
        
        // Auto remove after duration
        setTimeout(() => {
            this.removeNotification(notification);
        }, duration);
        
        return notification;
    }
    
    removeNotification(notification) {
        if (!notification || !document.body.contains(notification)) return;
        
            notification.classList.remove('show');
        notification.style.animation = 'slideOutRight 0.3s ease-in';
        
        setTimeout(() => {
            if (document.body.contains(notification)) {
                notification.remove();
            }
        }, 300);
    }
    
    getStatusText(status) {
        const statusMap = {
            'Available': 'Trống',
            'Occupied': 'Đang sử dụng',
            'Reserved': 'Đã đặt',
            'Maintenance': 'Bảo trì'
        };
        return statusMap[status] || status;
    }
    
    getPaymentStatusText(status) {
        const statusMap = {
            'Paid': 'Đã thanh toán',
            'Unpaid': 'Chưa thanh toán',
            'Partial': 'Thanh toán một phần'
        };
        return statusMap[status] || status;
    }
    
    // Helper function to safely format dates
    formatDate(dateString) {
        if (!dateString) return 'N/A';
        try {
            const date = new Date(dateString);
            if (isNaN(date.getTime())) return 'N/A';
            
            // Format as dd/MM/yyyy HH:mm
            const day = String(date.getDate()).padStart(2, '0');
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const year = date.getFullYear();
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            
            return `${day}/${month}/${year} ${hours}:${minutes}`;
        } catch (error) {
            console.warn('Error formatting date:', error);
            return 'N/A';
        }
    }
    
    // Helper function to safely format numbers
    formatNumber(number) {
        if (!number || isNaN(number)) return '0';
        return Number(number).toLocaleString('vi-VN');
    }
    
    // UI/UX Enhancement methods
    setupStatusAnimations() {
        // Status badge animations
        const statusBadges = document.querySelectorAll('.status');
        statusBadges.forEach(badge => {
            badge.addEventListener('click', this.handleStatusClick.bind(this));
        });
    }

    handleStatusClick(event) {
        const status = event.currentTarget;
        status.style.animation = 'bounce 0.6s ease-out';
        
        setTimeout(() => {
            status.style.animation = '';
        }, 600);
    }

    handleStatusHover(event) {
        const status = event.currentTarget;
        status.style.transform = 'scale(1.1)';
        status.style.boxShadow = '0 4px 15px rgba(0, 0, 0, 0.2)';
    }

    handleStatusLeave(event) {
        const status = event.currentTarget;
        status.style.transform = 'scale(1)';
        status.style.boxShadow = '';
    }

    setupModalEnhancements() {
        // Enhanced modal interactions
        const modals = document.querySelectorAll('.modal');
        modals.forEach(modal => {
            modal.addEventListener('show', this.handleModalShow.bind(this));
            modal.addEventListener('hide', this.handleModalHide.bind(this));
        });
    }

    handleModalShow(event) {
        const modal = event.target;
        modal.style.animation = 'fadeInUp 0.4s ease-out';
        
        // Add backdrop blur
        const backdrop = modal.querySelector('.modal-backdrop');
        if (backdrop) {
            backdrop.style.backdropFilter = 'blur(10px)';
        }
    }

    handleModalHide(event) {
        const modal = event.target;
        modal.style.animation = 'fadeOutDown 0.3s ease-in';
    }

    setupRealTimeUpdates() {
        // Real-time status updates simulation
        this.updateInterval = setInterval(() => {
            this.simulateStatusUpdates();
        }, 30000); // Update every 30 seconds
    }

    simulateStatusUpdates() {
        const statusBadges = document.querySelectorAll('.status');
        statusBadges.forEach(badge => {
            if (Math.random() < 0.1) { // 10% chance to update
                this.animateStatusChange(badge);
            }
        });
    }

    animateStatusChange(badge) {
        badge.style.animation = 'pulse 0.8s ease-out';
        badge.style.transform = 'scale(1.05)';
        
        setTimeout(() => {
            badge.style.animation = '';
            badge.style.transform = '';
        }, 800);
    }

    handleButtonHover(event) {
        const button = event.currentTarget;
        button.style.transform = 'translateY(-2px)';
        button.style.boxShadow = '0 6px 20px rgba(0, 128, 255, 0.3)';
    }

    handleButtonLeave(event) {
        const button = event.currentTarget;
        button.style.transform = 'translateY(0)';
        button.style.boxShadow = '';
    }

    handleButtonClick(event) {
        const button = event.currentTarget;
        button.style.animation = 'scaleIn 0.2s ease-out';
        
        setTimeout(() => {
            button.style.animation = '';
        }, 200);
    }

    handleRowHover(event) {
        const row = event.currentTarget;
        row.style.transform = 'translateX(8px)';
        row.style.boxShadow = '0 4px 12px rgba(0, 128, 255, 0.15)';
    }

    handleRowLeave(event) {
        const row = event.currentTarget;
        row.style.transform = 'translateX(0)';
        row.style.boxShadow = '';
    }

    setupPerformanceOptimizations() {
        // Intersection Observer for lazy loading
        this.setupIntersectionObserver();
        
        // Debounced scroll handler
        window.addEventListener('scroll', this.debounce(this.handleScroll.bind(this), 16));
        
        // Memory management
        this.setupMemoryManagement();
    }

    setupIntersectionObserver() {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('animate-fade-in-up');
                }
            });
        }, { threshold: 0.1 });

        // Observe all animatable elements
        document.querySelectorAll('.stat-card, .section-title, .room-table-container').forEach(el => {
            observer.observe(el);
        });
    }

    handleScroll() {
        // Scroll-based animations and optimizations
        const scrollY = window.scrollY;
        const toolbar = document.querySelector('.toolbar');
        
        if (toolbar) {
            if (scrollY > 100) {
                toolbar.style.boxShadow = '0 4px 20px rgba(0, 128, 255, 0.15)';
            } else {
                toolbar.style.boxShadow = '';
            }
        }
    }

    setupMemoryManagement() {
        // Clean up event listeners and intervals
        window.addEventListener('beforeunload', () => {
            if (this.updateInterval) {
                clearInterval(this.updateInterval);
            }
        });
    }
    

    // Public API methods
    refreshData() {
        // Refresh table data with animation
        const tableBody = document.querySelector('.table tbody');
        if (tableBody) {
            tableBody.style.opacity = '0.5';
            tableBody.style.transform = 'translateY(20px)';
            
            setTimeout(() => {
                // Simulate data refresh
                tableBody.style.opacity = '1';
                tableBody.style.transform = 'translateY(0)';
                tableBody.style.transition = 'all 0.3s ease-out';
            }, 500);
        }
    }

    exportData() {
        // Export functionality with loading animation
        const exportBtn = document.querySelector('[data-action="export"]');
        if (exportBtn) {
            exportBtn.style.animation = 'pulse 1s infinite';
            exportBtn.textContent = 'Đang xuất...';
            
            setTimeout(() => {
                exportBtn.style.animation = '';
                exportBtn.textContent = 'Xuất dữ liệu';
            }, 2000);
        }
    }
}

// Global Notification Manager for better performance
class NotificationManager {
    constructor() {
        this.stack = null;
        this.notifications = new Map();
        this.maxNotifications = 5;
        this.init();
    }
    
    init() {
        // Create notification stack
        this.stack = document.createElement('div');
        this.stack.id = 'notification-stack';
        this.stack.className = 'notification-stack';
        document.body.appendChild(this.stack);
        
        // Listen for page visibility changes
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                this.pauseAllAnimations();
            } else {
                this.resumeAllAnimations();
            }
        });
    }
    
    show(message, type = 'info', title = null, duration = 3000) {
        // Remove oldest notification if at max capacity
        if (this.notifications.size >= this.maxNotifications) {
            const oldest = this.notifications.values().next().value;
            this.remove(oldest);
        }
        
        const id = Date.now() + Math.random();
        const notification = this.createNotification(id, message, type, title, duration);
        
        this.notifications.set(id, notification);
        this.stack.appendChild(notification);
        
        // Trigger animation
        requestAnimationFrame(() => {
            notification.classList.add('show');
        });
        
        return id;
    }
    
    createNotification(id, message, type, title, duration) {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.dataset.id = id;
        
        const icons = {
            success: '✅',
            error: '❌',
            warning: '⚠️',
            info: 'ℹ️'
        };
        
        const icon = icons[type] || icons.info;
        
        notification.innerHTML = `
            <div class="notification-icon">${icon}</div>
            <div class="notification-content">
                ${title ? `<div class="notification-title">${title}</div>` : ''}
                <div class="notification-message">${message}</div>
            </div>
            <div class="notification-close" onclick="window.notificationManager.remove(${id})">×</div>
            <div class="notification-progress"></div>
        `;
        
        // Auto remove
        if (duration > 0) {
            setTimeout(() => this.remove(id), duration);
        }
        
        return notification;
    }
    
    remove(id) {
        const notification = this.notifications.get(id);
        if (!notification || !document.body.contains(notification)) return;
        
        notification.classList.remove('show');
        notification.style.animation = 'slideOutRight 0.3s ease-in';
        
        setTimeout(() => {
            if (document.body.contains(notification)) {
                notification.remove();
                this.notifications.delete(id);
            }
        }, 300);
    }
    
    clear() {
        this.notifications.forEach((notification, id) => {
            this.remove(id);
        });
    }
    
    pauseAllAnimations() {
        this.notifications.forEach(notification => {
            notification.style.animationPlayState = 'paused';
        });
    }
    
    resumeAllAnimations() {
        this.notifications.forEach(notification => {
            notification.style.animationPlayState = 'running';
        });
    }
}

// Initialize global notification manager
window.notificationManager = new NotificationManager();

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Initialize pagination first
    setTimeout(() => {
        console.log('🔧 Initializing pagination...');
        initializePagination();
        
        // Then initialize RoomTableManager (which includes animations)
    window.roomTableManager = new RoomTableManager();
        
        // Format all dates on page load
        formatAllDates();
        
        // Update room options with remaining capacity
        updateAllRoomOptions();
        console.log('✅ Room options updated with remaining capacity');
        
        // Update capacity badges with warning colors
        updateCapacityBadges();
        
        // Update data-room-id for existing table rows
        updateTableRoomIds();
        console.log('✅ Capacity badges updated with warning colors');
    }, 100);
    
    // Add global keyboard shortcuts
    document.addEventListener('keydown', (e) => {
        if (e.ctrlKey || e.metaKey) {
            switch(e.key) {
                case 'f':
                    e.preventDefault();
                    document.getElementById('searchInput')?.focus();
                    break;
                case 'r':
                    e.preventDefault();
                    window.roomTableManager.refreshData();
                    break;
                case 'n':
                    e.preventDefault();
                    window.notificationManager.clear();
                    break;
            }
        }
    });
    
    // Show welcome notification - DISABLED
    // setTimeout(() => {
    //     window.notificationManager.show(
    //         'Chào mừng đến với hệ thống quản lý phòng/bàn!',
    //         'success',
    //         'LiteFlow',
    //         5000
    //     );
    // }, 1000);
});

// Function to format all dates on the page
function formatAllDates() {
    const dateElements = document.querySelectorAll('.formatted-date[data-date]');
    dateElements.forEach(element => {
        const dateStr = element.getAttribute('data-date');
        if (dateStr) {
            const formattedDate = window.formatDate(dateStr);
            element.textContent = formattedDate;
        }
    });
}

// Function to update capacity badges with warning colors
function updateCapacityBadges() {
    console.log('🔄 Updating capacity badges...');
    
    const capacityBadges = document.querySelectorAll('.capacity-badge');
    
    capacityBadges.forEach(badge => {
        // Remove existing warning classes
        badge.classList.remove('low-capacity', 'critical-capacity');
        
        // Extract capacity number from text
        const text = badge.textContent.trim();
        const capacityMatch = text.match(/(\d+)/);
        
        if (capacityMatch) {
            const capacity = parseInt(capacityMatch[1]);
            
            // Apply warning colors based on capacity
            if (capacity <= 2) {
                badge.classList.add('critical-capacity');
                console.log('🔴 Critical capacity:', text, capacity);
            } else if (capacity <= 5) {
                badge.classList.add('low-capacity');
                console.log('🟡 Low capacity:', text, capacity);
            } else {
                console.log('🟢 Normal capacity:', text, capacity);
            }
        }
    });
    
    console.log('✅ Capacity badges updated');
}


// Function to adjust modal height based on content
function adjustModalHeight() {
    const modalContent = document.querySelector('.modal-content');
    const modalBody = document.querySelector('.modal-body');
    const roomLimitsInfo = document.getElementById('roomLimitsInfo');
    
    if (!modalContent || !modalBody) return;
    
    // Calculate content height
    const bodyHeight = modalBody.scrollHeight;
    const limitsHeight = roomLimitsInfo && roomLimitsInfo.classList.contains('show') ? roomLimitsInfo.offsetHeight : 0;
    const totalContentHeight = bodyHeight + limitsHeight + 100; // +100 for header and footer
    
    // Set minimum and maximum heights
    const minHeight = 300; // Reduced minimum height
    const maxHeight = window.innerHeight - 150; // More space available
    
    // Calculate optimal height
    let optimalHeight = Math.max(minHeight, totalContentHeight);
    optimalHeight = Math.min(optimalHeight, maxHeight);
    
    // Only apply height if content exceeds minimum
    if (totalContentHeight > minHeight) {
        modalContent.style.height = optimalHeight + 'px';
        modalContent.style.maxHeight = optimalHeight + 'px';
    } else {
        // Let CSS handle the default height
        modalContent.style.height = '';
        modalContent.style.maxHeight = '';
    }
    
    console.log('📏 Modal height adjusted:', {
        bodyHeight,
        limitsHeight,
        totalContentHeight,
        optimalHeight,
        minHeight,
        maxHeight
    });
}

// Function to update room limits display for edit mode (excluding current table being edited)
function updateRoomLimitsForEdit(tableId, roomId, currentCapacity) {
    const roomSelect = document.getElementById('roomId');
    const roomLimitsInfo = document.getElementById('roomLimitsInfo');
    const currentTableCountSpan = document.getElementById('currentTableCount');
    const maxTableCountSpan = document.getElementById('maxTableCount');
    const currentTotalCapacitySpan = document.getElementById('currentTotalCapacity');
    const maxTotalCapacitySpan = document.getElementById('maxTotalCapacity');
    
    if (!roomSelect || !roomLimitsInfo) {
        console.log('❌ Room select or roomLimitsInfo not found');
        return;
    }
    
    console.log('🔄 Updating room limits for edit mode:', { tableId, roomId, currentCapacity });
    
    // Update all room options with remaining capacity (excluding current table being edited)
    updateAllRoomOptionsForEdit(tableId);
    
    const selectedOption = roomSelect.options[roomSelect.selectedIndex];
    
    if (selectedOption && selectedOption.value) {
        const selectedRoomId = selectedOption.value;
        const selectedRoomName = selectedOption.getAttribute('data-room-name');
        const maxTableCount = parseInt(selectedOption.getAttribute('data-table-count')) || 0;
        const maxTotalCapacity = parseInt(selectedOption.getAttribute('data-total-capacity')) || 0;
        
        // Always exclude the table being edited from current counts
        // This shows the actual remaining capacity (not including the table being edited)
        const currentTableCount = getCurrentTableCountForRoomExcluding(selectedRoomId, tableId);
        const currentTotalCapacity = getCurrentTotalCapacityForRoomExcluding(selectedRoomId, tableId);
        
        // Calculate remaining
        const remainingTableCount = maxTableCount - currentTableCount;
        const remainingTotalCapacity = maxTotalCapacity - currentTotalCapacity;
        
        // Update display - show actual remaining capacity (excluding current table)
        currentTableCountSpan.textContent = remainingTableCount;
        maxTableCountSpan.textContent = maxTableCount;
        currentTotalCapacitySpan.textContent = remainingTotalCapacity;
        maxTotalCapacitySpan.textContent = maxTotalCapacity;
        
        // Show/hide based on limits
        if (maxTableCount > 0 || maxTotalCapacity > 0) {
            console.log('✅ Showing room limits info for edit mode');
            roomLimitsInfo.classList.add('show');
            
            // Update remaining counts
            const remainingTableCountSpan = document.getElementById('remainingTableCount');
            const remainingTotalCapacitySpan = document.getElementById('remainingTotalCapacity');
            
            if (remainingTableCountSpan) {
                remainingTableCountSpan.textContent = remainingTableCount;
            }
            if (remainingTotalCapacitySpan) {
                remainingTotalCapacitySpan.textContent = remainingTotalCapacity;
            }
        } else {
            console.log('❌ Hiding room limits info (no limits set)');
            roomLimitsInfo.classList.remove('show');
        }
    } else {
        console.log('❌ Hiding room limits info (no room selected)');
        roomLimitsInfo.classList.remove('show');
    }
}

// Function to update all room options for edit mode
function updateAllRoomOptionsForEdit(excludeTableId) {
    const roomSelect = document.getElementById('roomId');
    if (!roomSelect) return;
    
    console.log('🔄 Updating all room options for edit mode, excluding table:', excludeTableId);
    
    for (let i = 1; i < roomSelect.options.length; i++) { // Skip first option (empty)
        const option = roomSelect.options[i];
        const roomId = option.value;
        const roomName = option.getAttribute('data-room-name');
        const maxTableCount = parseInt(option.getAttribute('data-table-count')) || 0;
        const maxTotalCapacity = parseInt(option.getAttribute('data-total-capacity')) || 0;
        
        // Always exclude the table being edited from current counts
        // This shows the actual remaining capacity (not including the table being edited)
        const currentTableCount = getCurrentTableCountForRoomExcluding(roomId, excludeTableId);
        const currentTotalCapacity = getCurrentTotalCapacityForRoomExcluding(roomId, excludeTableId);
        
        console.log(`📊 Room "${roomName}" (ID: ${roomId}) for edit mode:`, {
            maxTableCount: maxTableCount,
            maxTotalCapacity: maxTotalCapacity,
            currentTableCount: currentTableCount,
            currentTotalCapacity: currentTotalCapacity,
            excludeTableId: excludeTableId
        });
        
        // Calculate remaining/available capacity
        const remainingTableCount = maxTableCount - currentTableCount;
        const remainingTotalCapacity = maxTotalCapacity - currentTotalCapacity;
        
        // Update option text - show actual remaining capacity (excluding current table)
        if (remainingTableCount <= 0 && remainingTotalCapacity <= 0) {
            option.textContent = `${roomName} (Đã đầy)`;
            option.style.color = '#dc3545';
        } else if (remainingTableCount <= 0) {
            option.textContent = `${roomName} (Còn lại ${remainingTotalCapacity} người)`;
            option.style.color = '#ffc107';
        } else if (remainingTotalCapacity <= 0) {
            option.textContent = `${roomName} (Còn lại ${remainingTableCount} bàn)`;
            option.style.color = '#ffc107';
        } else {
            option.textContent = `${roomName} (Còn lại ${remainingTableCount} bàn, ${remainingTotalCapacity} người)`;
            option.style.color = '#28a745';
        }
    }
}

// Helper function to get current table count for a room excluding specific table
function getCurrentTableCountForRoomExcluding(roomId, excludeTableId) {
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) return 0;
    
    const table = tablesContainer.querySelector('.table tbody');
    if (!table) return 0;
    
    const rows = table.querySelectorAll('tr');
    let count = 0;
    
    // Get room info to match by name
    const room = getRoomById(roomId);
    if (!room) return 0;
    
    for (let row of rows) {
        const roomCell = row.querySelector('td:nth-child(3)'); // Room column
        const editButton = row.querySelector('button[onclick*="editTable"]');
        
        if (roomCell && editButton) {
            const roomBadge = roomCell.querySelector('.room-badge');
            if (roomBadge) {
                const roomName = roomBadge.textContent.trim();
                if (roomName === room.name) {
                    // Check if this is the table being excluded
                    const onclickAttr = editButton.getAttribute('onclick');
                    const tableIdMatch = onclickAttr.match(/editTable\('([^']+)'\)/);
                    if (tableIdMatch && tableIdMatch[1] === excludeTableId) {
                        console.log('⏭️ Excluding table from count:', excludeTableId);
                        continue; // Skip this table
                    }
                    count++;
                }
            }
        }
    }
    
    console.log(`📊 Current table count for room "${room.name}" (ID: ${roomId}) excluding table ${excludeTableId}: ${count}`);
    return count;
}

// Helper function to get current total capacity for a room excluding specific table
function getCurrentTotalCapacityForRoomExcluding(roomId, excludeTableId) {
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) return 0;
    
    const table = tablesContainer.querySelector('.table tbody');
    if (!table) return 0;
    
    const rows = table.querySelectorAll('tr');
    let totalCapacity = 0;
    
    // Get room info to match by name
    const room = getRoomById(roomId);
    if (!room) {
        return 0;
    }
    
    for (let row of rows) {
        const roomCell = row.querySelector('td:nth-child(3)'); // Room column
        const capacityCell = row.querySelector('td:nth-child(4)'); // Capacity column
        const editButton = row.querySelector('button[onclick*="editTable"]');
        
        if (roomCell && capacityCell && editButton) {
            const roomBadge = roomCell.querySelector('.room-badge');
            if (roomBadge) {
                const roomName = roomBadge.textContent.trim();
                if (roomName === room.name) {
                    // Check if this is the table being excluded
                    const onclickAttr = editButton.getAttribute('onclick');
                    const tableIdMatch = onclickAttr.match(/editTable\('([^']+)'\)/);
                    if (tableIdMatch && tableIdMatch[1] === excludeTableId) {
                        continue; // Skip this table
                    }
                    
                    const capacityText = capacityCell.textContent.trim();
                    const capacityMatch = capacityText.match(/(\d+)/);
                    if (capacityMatch) {
                        const capacity = parseInt(capacityMatch[1]);
                        totalCapacity += capacity;
                    }
                }
            }
        }
    }
    
    return totalCapacity;
}

// Function to update room limits display and dropdown text
window.updateRoomLimits = function() {
    const roomSelect = document.getElementById('roomId');
    const roomLimitsInfo = document.getElementById('roomLimitsInfo');
    const currentTableCountSpan = document.getElementById('currentTableCount');
    const maxTableCountSpan = document.getElementById('maxTableCount');
    const currentTotalCapacitySpan = document.getElementById('currentTotalCapacity');
    const maxTotalCapacitySpan = document.getElementById('maxTotalCapacity');
    
    if (!roomSelect || !roomLimitsInfo) {
        console.log('❌ Room select or roomLimitsInfo not found');
        return;
    }
    
    console.log('🔍 RoomLimitsInfo element:', roomLimitsInfo);
    console.log('🔍 RoomLimitsInfo classes:', roomLimitsInfo.classList.toString());
    
    // Update all room options with remaining capacity
    updateAllRoomOptions();
    
    const selectedOption = roomSelect.options[roomSelect.selectedIndex];
    
    if (selectedOption && selectedOption.value) {
        const roomId = selectedOption.value;
        const maxTableCount = parseInt(selectedOption.getAttribute('data-table-count')) || 0;
        const maxTotalCapacity = parseInt(selectedOption.getAttribute('data-total-capacity')) || 0;
        
        // Get current counts
        const currentTableCount = getCurrentTableCountForRoom(roomId);
        const currentTotalCapacity = getCurrentTotalCapacityForRoom(roomId);
        
        // Calculate remaining
        const remainingTableCount = maxTableCount - currentTableCount;
        const remainingTotalCapacity = maxTotalCapacity - currentTotalCapacity;
        
        // Update display
        currentTableCountSpan.textContent = currentTableCount;
        maxTableCountSpan.textContent = maxTableCount;
        currentTotalCapacitySpan.textContent = currentTotalCapacity;
        maxTotalCapacitySpan.textContent = maxTotalCapacity;
        
        // Show/hide based on limits
        if (maxTableCount > 0 || maxTotalCapacity > 0) {
            console.log('✅ Showing room limits info');
            roomLimitsInfo.classList.add('show');
            
            // Add warning colors if approaching limits
            if (currentTableCount >= maxTableCount) {
                currentTableCountSpan.style.color = '#dc3545';
                maxTableCountSpan.style.color = '#dc3545';
            } else if (currentTableCount >= maxTableCount * 0.8) {
                currentTableCountSpan.style.color = '#ffc107';
                maxTableCountSpan.style.color = '#ffc107';
            } else {
                currentTableCountSpan.style.color = '#28a745';
                maxTableCountSpan.style.color = '#28a745';
            }
            
            if (currentTotalCapacity >= maxTotalCapacity) {
                currentTotalCapacitySpan.style.color = '#dc3545';
                maxTotalCapacitySpan.style.color = '#dc3545';
            } else if (currentTotalCapacity >= maxTotalCapacity * 0.8) {
                currentTotalCapacitySpan.style.color = '#ffc107';
                maxTotalCapacitySpan.style.color = '#ffc107';
            } else {
                currentTotalCapacitySpan.style.color = '#28a745';
                maxTotalCapacitySpan.style.color = '#28a745';
            }
        } else {
            console.log('❌ Hiding room limits info - no limits');
            roomLimitsInfo.classList.remove('show');
        }
    } else {
        console.log('❌ Hiding room limits info - no room selected');
        roomLimitsInfo.classList.remove('show');
    }
    
    // Adjust modal height after showing/hiding room limits
    setTimeout(() => {
        adjustModalHeight();
    }, 300); // Wait for CSS transition to complete
};

// Function to show empty state for rooms
function showRoomsEmptyState() {
    console.log('🏠 Showing rooms empty state');
    
    const roomsContainer = document.querySelector('.room-table-container');
    if (!roomsContainer) {
        console.error('Rooms container not found');
        return;
    }
    
    // Hide the table
    const existingTable = roomsContainer.querySelector('.table');
    if (existingTable) {
        existingTable.style.display = 'none';
    }
    
    // Hide pagination container
    const paginationContainer = roomsContainer.querySelector('.pagination-container');
    if (paginationContainer) {
        paginationContainer.style.display = 'none';
    }
    
    // Show existing empty state if it exists
    const existingEmptyState = roomsContainer.querySelector('.empty-state');
    if (existingEmptyState) {
        existingEmptyState.style.display = 'block';
        console.log('✅ Existing rooms empty state shown');
    } else {
        console.warn('⚠️ No existing empty state found for rooms');
    }
}

// Function to show empty state for tables
function showTablesEmptyState() {
    console.log('🪑 Showing tables empty state');
    
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) {
        console.error('Tables container not found');
        return;
    }
    
    // Hide the table
    const existingTable = tablesContainer.querySelector('.table');
    if (existingTable) {
        existingTable.style.display = 'none';
    }
    
    // Hide pagination container
    const paginationContainer = tablesContainer.querySelector('.pagination-container');
    if (paginationContainer) {
        paginationContainer.style.display = 'none';
    }
    
    // Show existing empty state if it exists
    const existingEmptyState = tablesContainer.querySelector('.empty-state');
    if (existingEmptyState) {
        existingEmptyState.style.display = 'block';
        console.log('✅ Existing tables empty state shown');
    } else {
        console.warn('⚠️ No existing empty state found for tables');
    }
}

// Function to hide empty state for rooms
function hideRoomsEmptyState() {
    console.log('🏠 Hiding rooms empty state');
    
    const roomsContainer = document.querySelector('.room-table-container');
    if (!roomsContainer) {
        console.error('Rooms container not found');
        return;
    }
    
    // Show the table
    const existingTable = roomsContainer.querySelector('.table');
    if (existingTable) {
        existingTable.style.display = '';
    }
    
    // Show pagination container
    const paginationContainer = roomsContainer.querySelector('.pagination-container');
    if (paginationContainer) {
        paginationContainer.style.display = '';
    }
    
    // Hide existing empty state if it exists
    const existingEmptyState = roomsContainer.querySelector('.empty-state');
    if (existingEmptyState) {
        existingEmptyState.style.display = 'none';
    }
    
    console.log('✅ Rooms empty state hidden');
}

// Function to hide empty state for tables
function hideTablesEmptyState() {
    console.log('🪑 Hiding tables empty state');
    
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) {
        console.error('Tables container not found');
        return;
    }
    
    // Show the table
    const existingTable = tablesContainer.querySelector('.table');
    if (existingTable) {
        existingTable.style.display = '';
    }
    
    // Show pagination container
    const paginationContainer = tablesContainer.querySelector('.pagination-container');
    if (paginationContainer) {
        paginationContainer.style.display = '';
    }
    
    // Hide existing empty state if it exists
    const existingEmptyState = tablesContainer.querySelector('.empty-state');
    if (existingEmptyState) {
        existingEmptyState.style.display = 'none';
    }
    
    console.log('✅ Tables empty state hidden');
}

// Function to update room dropdown with new room
function updateRoomDropdownWithNewRoom(roomId, roomName, tableCount, totalCapacity) {
    const roomSelect = document.getElementById('roomId');
    if (!roomSelect) return;
    
    // Check if room already exists in dropdown to avoid duplicate
    for (let i = 1; i < roomSelect.options.length; i++) {
        if (roomSelect.options[i].value === roomId) {
            console.log('⚠️ Room already exists in dropdown, skipping duplicate:', roomName);
            return;
        }
    }
    
    // Create new option element
    const newOption = document.createElement('option');
    newOption.value = roomId;
    newOption.setAttribute('data-table-count', tableCount);
    newOption.setAttribute('data-total-capacity', totalCapacity);
    newOption.setAttribute('data-room-name', roomName);
    
    // Calculate remaining capacity (new room has 0 current usage)
    const remainingTableCount = tableCount;
    const remainingTotalCapacity = totalCapacity;
    
    // Set option text and styling
    if (remainingTableCount <= 0 && remainingTotalCapacity <= 0) {
        newOption.textContent = `${roomName} (Đã đầy)`;
        newOption.style.color = '#dc3545';
        newOption.disabled = true;
    } else {
        newOption.textContent = `${roomName} (Còn lại ${remainingTableCount} bàn và ${remainingTotalCapacity} sức chứa)`;
        newOption.style.color = remainingTableCount <= 1 || remainingTotalCapacity <= 5 ? '#ffc107' : '#28a745';
        newOption.disabled = false;
    }
    
    // Insert new option after the first option (empty option)
    roomSelect.insertBefore(newOption, roomSelect.options[1]);
    
    console.log('✅ Room dropdown updated with new room:', roomName);
}

// Function to update room dropdown when room is updated
function updateRoomDropdownWithUpdatedRoom(roomId, roomName, tableCount, totalCapacity) {
    const roomSelect = document.getElementById('roomId');
    if (!roomSelect) return;
    
    // Find the existing option for this room
    for (let i = 1; i < roomSelect.options.length; i++) { // Skip first option (empty)
        const option = roomSelect.options[i];
        if (option.value === roomId) {
            // Update the option attributes
            option.setAttribute('data-table-count', tableCount);
            option.setAttribute('data-total-capacity', totalCapacity);
            option.setAttribute('data-room-name', roomName);
            
            // Get current counts
            const currentTableCount = getCurrentTableCountForRoom(roomId);
            const currentTotalCapacity = getCurrentTotalCapacityForRoom(roomId);
            
            // Calculate remaining
            const remainingTableCount = tableCount - currentTableCount;
            const remainingTotalCapacity = totalCapacity - currentTotalCapacity;
            
            // Update option text and styling
            if (remainingTableCount <= 0 && remainingTotalCapacity <= 0) {
                option.textContent = `${roomName} (Đã đầy)`;
                option.style.color = '#dc3545';
                option.disabled = true;
            } else {
                option.textContent = `${roomName} (Còn lại ${remainingTableCount} bàn và ${remainingTotalCapacity} sức chứa)`;
                option.style.color = remainingTableCount <= 1 || remainingTotalCapacity <= 5 ? '#ffc107' : '#28a745';
                option.disabled = false;
            }
            
            console.log('✅ Room dropdown updated for edited room:', roomName);
            break;
        }
    }
}

// Function to remove room from dropdown when deleted
function removeRoomFromDropdown(roomId) {
    const roomSelect = document.getElementById('roomId');
    if (!roomSelect) return;
    
    // Find and remove the option for this room
    for (let i = 1; i < roomSelect.options.length; i++) { // Skip first option (empty)
        const option = roomSelect.options[i];
        if (option.value === roomId) {
            roomSelect.remove(i);
            console.log('✅ Room removed from dropdown:', roomId);
            break;
        }
    }
}

// Function to clear and rebuild dropdown from rooms list
function rebuildRoomDropdown() {
    const roomSelect = document.getElementById('roomId');
    if (!roomSelect) return;
    
    console.log('🔄 Rebuilding room dropdown...');
    
    // Clear all options except the first one (empty option)
    while (roomSelect.options.length > 1) {
        roomSelect.remove(1);
    }
    
    // Get all rooms from the table
    const roomsContainer = document.querySelector('.room-table-container');
    const roomsEmptyState = roomsContainer?.querySelector('.empty-state');
    const isRoomsEmptyStateVisible = roomsEmptyState && roomsEmptyState.style.display !== 'none';
    
    if (isRoomsEmptyStateVisible) {
        console.log('🔄 Rooms in empty state, skipping dropdown rebuild');
        return;
    }
    
    const roomsTableBody = roomsContainer ? roomsContainer.querySelector('tbody') : null;
    if (!roomsTableBody) {
        console.log('🔄 Rooms table body not found, likely in empty state');
        return;
    }
    
    const rows = roomsTableBody.querySelectorAll('tr[data-room-id]');
    
    rows.forEach(row => {
        const roomId = row.getAttribute('data-room-id');
        const nameCell = row.querySelector('.room-name strong');
        const tableCountCell = row.querySelector('.table-count-badge');
        const capacityCell = row.querySelector('.capacity-badge');
        
        if (nameCell && tableCountCell && capacityCell) {
            const roomName = nameCell.textContent.trim();
            
            // Extract numbers from badges
            const tableCountMatch = tableCountCell.textContent.match(/(\d+)/);
            const capacityMatch = capacityCell.textContent.match(/(\d+)/);
            
            const tableCount = tableCountMatch ? parseInt(tableCountMatch[1]) : 0;
            const totalCapacity = capacityMatch ? parseInt(capacityMatch[1]) : 0;
            
            // Create new option
            const newOption = document.createElement('option');
            newOption.value = roomId;
            newOption.setAttribute('data-table-count', tableCount);
            newOption.setAttribute('data-total-capacity', totalCapacity);
            newOption.setAttribute('data-room-name', roomName);
            
            // Calculate remaining capacity
            const currentTableCount = getCurrentTableCountForRoom(roomId);
            const currentTotalCapacity = getCurrentTotalCapacityForRoom(roomId);
            const remainingTableCount = tableCount - currentTableCount;
            const remainingTotalCapacity = totalCapacity - currentTotalCapacity;
            
            // Set option text and styling
            if (remainingTableCount <= 0 && remainingTotalCapacity <= 0) {
                newOption.textContent = `${roomName} (Đã đầy)`;
                newOption.style.color = '#dc3545';
                newOption.disabled = true;
            } else {
                newOption.textContent = `${roomName} (Còn lại ${remainingTableCount} bàn và ${remainingTotalCapacity} sức chứa)`;
                newOption.style.color = remainingTableCount <= 1 || remainingTotalCapacity <= 5 ? '#ffc107' : '#28a745';
                newOption.disabled = false;
            }
            
            roomSelect.appendChild(newOption);
        }
    });
    
    console.log('✅ Room dropdown rebuilt with', rows.length, 'rooms');
}


// Function to update all room options with remaining capacity
function updateAllRoomOptions() {
    const roomSelect = document.getElementById('roomId');
    if (!roomSelect) return;
    
    console.log('🔄 Updating all room options...');
    
    for (let i = 1; i < roomSelect.options.length; i++) { // Skip first option (empty)
        const option = roomSelect.options[i];
        const roomId = option.value;
        const roomName = option.getAttribute('data-room-name');
        const maxTableCount = parseInt(option.getAttribute('data-table-count')) || 0;
        const maxTotalCapacity = parseInt(option.getAttribute('data-total-capacity')) || 0;
        
        // Get current counts
        const currentTableCount = getCurrentTableCountForRoom(roomId);
        const currentTotalCapacity = getCurrentTotalCapacityForRoom(roomId);
        
        console.log(`📊 Room "${roomName}" (ID: ${roomId}):`, {
            maxTableCount: maxTableCount,
            maxTotalCapacity: maxTotalCapacity,
            currentTableCount: currentTableCount,
            currentTotalCapacity: currentTotalCapacity
        });
        
        // Calculate remaining
        const remainingTableCount = maxTableCount - currentTableCount;
        const remainingTotalCapacity = maxTotalCapacity - currentTotalCapacity;
        
        // Update option text
        if (remainingTableCount <= 0 && remainingTotalCapacity <= 0) {
            option.textContent = `${roomName} (Đã đầy)`;
            option.style.color = '#dc3545';
            option.disabled = true;
        } else {
            option.textContent = `${roomName} (Còn lại ${remainingTableCount} bàn và ${remainingTotalCapacity} sức chứa)`;
            option.style.color = remainingTableCount <= 1 || remainingTotalCapacity <= 5 ? '#ffc107' : '#28a745';
            option.disabled = false;
        }
    }
    
    console.log('✅ All room options updated');
}

// Function to validate capacity input against room limits
window.validateCapacityAgainstRoomLimits = function(excludeTableId = null, roomSelectId = 'roomId', capacityInputId = 'capacity') {
    const roomSelect = document.getElementById(roomSelectId);
    const capacityInput = document.getElementById(capacityInputId);
    
    if (!roomSelect || !capacityInput) return true;
    
    const selectedOption = roomSelect.options[roomSelect.selectedIndex];
    if (!selectedOption || !selectedOption.value) return true;
    
    const roomId = selectedOption.value;
    const maxTotalCapacity = parseInt(selectedOption.getAttribute('data-total-capacity')) || 0;
    const newCapacity = parseInt(capacityInput.value) || 0;
    
    if (maxTotalCapacity > 0) {
        // Get current capacity excluding the table being edited
        const currentTotalCapacityExcluding = excludeTableId 
            ? getCurrentTotalCapacityForRoomExcluding(roomId, excludeTableId)
            : getCurrentTotalCapacityForRoom(roomId);
            
        // Get current capacity of the table being edited
        const currentTableCapacity = excludeTableId 
            ? getCurrentTableCapacityFromDOM(excludeTableId)
            : 0;
            
        // Calculate total capacity after edit: excluding + new capacity
        const totalCapacityAfterEdit = currentTotalCapacityExcluding + newCapacity;
        
        if (totalCapacityAfterEdit > maxTotalCapacity) {
            const remainingCapacity = maxTotalCapacity - currentTotalCapacityExcluding;
            window.notificationManager?.show(
                `Sức chứa vượt quá giới hạn phòng. Còn lại ${remainingCapacity} người.`, 
                'error', 
                'Vượt quá giới hạn'
            );
            capacityInput.focus();
            return false;
        }
    }
    
    return true;
};

// Helper function to get room by ID
function getRoomById(roomId) {
    const roomsContainer = document.querySelectorAll('.room-table-container')[0];
    if (!roomsContainer) return null;
    
    const table = roomsContainer.querySelector('.table tbody');
    if (!table) return null;
    
    const rows = table.querySelectorAll('tr');
    for (let row of rows) {
        const editButton = row.querySelector('button[onclick*="editRoom"]');
        if (editButton) {
            const onclickAttr = editButton.getAttribute('onclick');
            const roomIdMatch = onclickAttr.match(/editRoom\('([^']+)'\)/);
            if (roomIdMatch && roomIdMatch[1] === roomId) {
                const nameCell = row.querySelector('td:first-child');
                const tableCountCell = row.querySelector('td:nth-child(4)');
                const totalCapacityCell = row.querySelector('td:nth-child(5)');
                
                if (nameCell && tableCountCell && totalCapacityCell) {
                    const name = nameCell.textContent.trim();
                    const tableCountText = tableCountCell.textContent.trim();
                    const totalCapacityText = totalCapacityCell.textContent.trim();
                    
                    // Extract numbers from "Tối đa X bàn" and "Tối đa X người"
                    const tableCountMatch = tableCountText.match(/Tối đa (\d+) bàn/);
                    const totalCapacityMatch = totalCapacityText.match(/Tối đa (\d+) người/);
                    
                    return {
                        roomId: roomId,
                        name: name,
                        tableCount: tableCountMatch ? parseInt(tableCountMatch[1]) : 0,
                        totalCapacity: totalCapacityMatch ? parseInt(totalCapacityMatch[1]) : 0
                    };
                }
            }
        }
    }
    return null;
}

// Helper function to get current table count for a room
function getCurrentTableCountForRoom(roomId) {
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) return 0;
    
    const table = tablesContainer.querySelector('.table tbody');
    if (!table) return 0;
    
    const rows = table.querySelectorAll('tr');
    let count = 0;
    
    // Get room info to match by name
    const room = getRoomById(roomId);
    if (!room) return 0;
    
    for (let row of rows) {
        const roomCell = row.querySelector('td:nth-child(3)'); // Room column
        if (roomCell) {
            const roomBadge = roomCell.querySelector('.room-badge');
            if (roomBadge) {
                const roomName = roomBadge.textContent.trim();
                if (roomName === room.name) {
                count++;
                }
            }
        }
    }
    
    console.log(`📊 Current table count for room "${room.name}" (ID: ${roomId}): ${count}`);
    return count;
}

// Helper function to get current total capacity for a room
function getCurrentTotalCapacityForRoom(roomId) {
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) return 0;
    
    const table = tablesContainer.querySelector('.table tbody');
    if (!table) return 0;
    
    const rows = table.querySelectorAll('tr');
    let totalCapacity = 0;
    
    // Get room info to match by name
    const room = getRoomById(roomId);
    if (!room) return 0;
    
    for (let row of rows) {
        const roomCell = row.querySelector('td:nth-child(3)'); // Room column
        const capacityCell = row.querySelector('td:nth-child(4)'); // Capacity column
        
        if (roomCell && capacityCell) {
            const roomBadge = roomCell.querySelector('.room-badge');
            if (roomBadge) {
                const roomName = roomBadge.textContent.trim();
                if (roomName === room.name) {
            const capacityText = capacityCell.textContent.trim();
                const capacityMatch = capacityText.match(/(\d+)/);
                if (capacityMatch) {
                        totalCapacity += parseInt(capacityMatch[1]);
                    }
                }
            }
        }
    }
    
    console.log(`📊 Current total capacity for room "${room.name}" (ID: ${roomId}): ${totalCapacity}`);
    return totalCapacity;
}

// Helper function to get table by ID
function getTableById(tableId) {
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    if (!tablesContainer) return null;
    
    const table = tablesContainer.querySelector('.table tbody');
    if (!table) return null;
    
    const rows = table.querySelectorAll('tr');
    for (let row of rows) {
        const editButton = row.querySelector('button[onclick*="editTable"]');
        if (editButton) {
            const onclickAttr = editButton.getAttribute('onclick');
            const tableIdMatch = onclickAttr.match(/editTable\('([^']+)'\)/);
            if (tableIdMatch && tableIdMatch[1] === tableId) {
                const tableNumberCell = row.querySelector('td:first-child');
                const tableNameCell = row.querySelector('td:nth-child(2)');
                const roomNameCell = row.querySelector('td:nth-child(3)');
                const capacityCell = row.querySelector('td:nth-child(4)');
                
                if (tableNumberCell && tableNameCell && capacityCell) {
                    const tableNumber = tableNumberCell.textContent.trim();
                    const tableName = tableNameCell.textContent.trim();
                    const roomName = roomNameCell ? roomNameCell.textContent.trim() : '';
                    const capacityText = capacityCell.textContent.trim();
                    
                    // Extract capacity number from text
                    const capacityMatch = capacityText.match(/(\d+)/);
                    const capacity = capacityMatch ? parseInt(capacityMatch[1]) : 0;
                    
                    return {
                        tableId: tableId,
                        tableNumber: tableNumber,
                        tableName: tableName,
                        roomName: roomName,
                        capacity: capacity
                    };
                }
            }
        }
    }
    return null;
}


// ==================== PAGINATION SYSTEM ====================

// Pagination state
let roomsPagination = {
    currentPage: 1,
    itemsPerPage: 5,
    totalItems: 0,
    totalPages: 0
};

let tablesPagination = {
    currentPage: 1,
    itemsPerPage: 5,
    totalItems: 0,
    totalPages: 0
};

// Initialize pagination
function initializePagination() {
    console.log('🔢 Initializing pagination...');
    
    // Debug: Check all room-table-container elements
    const allContainers = document.querySelectorAll('.room-table-container');
    console.log('🔢 Found containers:', allContainers.length);
    allContainers.forEach((container, index) => {
        const title = container.querySelector('.section-title');
        const tbody = container.querySelector('tbody');
        const emptyState = container.querySelector('.empty-state');
        const isEmptyStateVisible = emptyState && emptyState.style.display !== 'none';
        console.log(`🔢 Container ${index + 1}:`, {
            title: title ? title.textContent : 'No title',
            hasTbody: !!tbody,
            hasEmptyState: !!emptyState,
            isEmptyStateVisible: isEmptyStateVisible,
            rowCount: tbody ? tbody.querySelectorAll('tr').length : 0
        });
    });
    
    // Initialize rooms pagination - check empty state first
    const roomsContainer = document.querySelector('.room-table-container');
    const roomsEmptyState = roomsContainer?.querySelector('.empty-state');
    const isRoomsEmptyStateVisible = roomsEmptyState && roomsEmptyState.style.display !== 'none';
    
    if (isRoomsEmptyStateVisible) {
        console.log('🔢 Rooms in empty state, skipping pagination initialization');
        roomsPagination.totalItems = 0;
        roomsPagination.totalPages = 0;
        roomsPagination.currentPage = 1;
    } else {
        // Normal case: initialize with tbody
        const roomsTableBody = roomsContainer ? roomsContainer.querySelector('tbody') : null;
        
        if (roomsTableBody) {
            const roomsRows = roomsTableBody.querySelectorAll('tr');
            console.log('🔢 Found rooms rows:', roomsRows.length);
            console.log('🔢 Rooms rows details:', Array.from(roomsRows).map((row, index) => ({
                index: index + 1,
                roomId: row.getAttribute('data-room-id'),
                roomName: row.querySelector('.room-name') ? row.querySelector('.room-name').textContent.trim() : 'No name'
            })));
            
            roomsPagination.totalItems = roomsRows.length;
            roomsPagination.totalPages = Math.ceil(roomsPagination.totalItems / roomsPagination.itemsPerPage);
            console.log('🔢 Rooms pagination:', roomsPagination);
            updateRoomsPagination();
            showRoomsPage(1);
            console.log('🔢 Rooms page 1 shown');
        } else {
            console.log('🔢 Rooms table body not found, likely in empty state');
            roomsPagination.totalItems = 0;
            roomsPagination.totalPages = 0;
            roomsPagination.currentPage = 1;
        }
    }
    
    // Initialize tables pagination - check empty state first
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    const tablesEmptyState = tablesContainer?.querySelector('.empty-state');
    const isTablesEmptyStateVisible = tablesEmptyState && tablesEmptyState.style.display !== 'none';
    
    if (isTablesEmptyStateVisible) {
        console.log('🔢 Tables in empty state, skipping pagination initialization');
        tablesPagination.totalItems = 0;
        tablesPagination.totalPages = 0;
        tablesPagination.currentPage = 1;
    } else {
        // Normal case: initialize with tbody
        const tablesTableBody = tablesContainer ? tablesContainer.querySelector('tbody') : null;
        
        if (tablesTableBody) {
            const tablesRows = tablesTableBody.querySelectorAll('tr');
            console.log('🔢 Found tables rows:', tablesRows.length);
            tablesPagination.totalItems = tablesRows.length;
            tablesPagination.totalPages = Math.ceil(tablesPagination.totalItems / tablesPagination.itemsPerPage);
            console.log('🔢 Tables pagination:', tablesPagination);
            updateTablesPagination();
            showTablesPage(1);
            console.log('🔢 Tables page 1 shown');
        } else {
            console.log('🔢 Tables table body not found, likely in empty state');
            tablesPagination.totalItems = 0;
            tablesPagination.totalPages = 0;
            tablesPagination.currentPage = 1;
        }
    }
    
    console.log('✅ Pagination initialized:', { roomsPagination, tablesPagination });
}

// Update rooms pagination UI
function updateRoomsPagination() {
    const pageInfo = document.getElementById('roomsPageInfo');
    const prevBtn = document.getElementById('roomsPrevBtn');
    const nextBtn = document.getElementById('roomsNextBtn');
    const pageNumbers = document.getElementById('roomsPageNumbers');
    
    if (!pageInfo || !prevBtn || !nextBtn || !pageNumbers) return;
    
    // Update page info
    pageInfo.textContent = `Trang ${roomsPagination.currentPage} / ${roomsPagination.totalPages}`;
    
    // Update prev/next buttons
    prevBtn.disabled = roomsPagination.currentPage <= 1;
    nextBtn.disabled = roomsPagination.currentPage >= roomsPagination.totalPages;
    
    // Update page numbers
    pageNumbers.innerHTML = '';
    const startPage = Math.max(1, roomsPagination.currentPage - 2);
    const endPage = Math.min(roomsPagination.totalPages, roomsPagination.currentPage + 2);
    
    // Add ellipsis if needed
    if (startPage > 1) {
        const firstBtn = document.createElement('button');
        firstBtn.className = 'pagination-number';
        firstBtn.textContent = '1';
        firstBtn.onclick = () => goToRoomsPage(1);
        pageNumbers.appendChild(firstBtn);
        
        if (startPage > 2) {
            const ellipsis = document.createElement('span');
            ellipsis.className = 'pagination-ellipsis';
            ellipsis.textContent = '...';
            pageNumbers.appendChild(ellipsis);
        }
    }
    
    // Add page numbers
    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.className = `pagination-number ${i === roomsPagination.currentPage ? 'active' : ''}`;
        pageBtn.textContent = i;
        pageBtn.onclick = () => goToRoomsPage(i);
        pageNumbers.appendChild(pageBtn);
    }
    
    // Add ellipsis if needed
    if (endPage < roomsPagination.totalPages) {
        if (endPage < roomsPagination.totalPages - 1) {
            const ellipsis = document.createElement('span');
            ellipsis.className = 'pagination-ellipsis';
            ellipsis.textContent = '...';
            pageNumbers.appendChild(ellipsis);
        }
        
        const lastBtn = document.createElement('button');
        lastBtn.className = 'pagination-number';
        lastBtn.textContent = roomsPagination.totalPages;
        lastBtn.onclick = () => goToRoomsPage(roomsPagination.totalPages);
        pageNumbers.appendChild(lastBtn);
    }
}

// Update tables pagination UI
function updateTablesPagination() {
    const pageInfo = document.getElementById('tablesPageInfo');
    const prevBtn = document.getElementById('tablesPrevBtn');
    const nextBtn = document.getElementById('tablesNextBtn');
    const pageNumbers = document.getElementById('tablesPageNumbers');
    
    if (!pageInfo || !prevBtn || !nextBtn || !pageNumbers) return;
    
    // Update page info
    pageInfo.textContent = `Trang ${tablesPagination.currentPage} / ${tablesPagination.totalPages}`;
    
    // Update prev/next buttons
    prevBtn.disabled = tablesPagination.currentPage <= 1;
    nextBtn.disabled = tablesPagination.currentPage >= tablesPagination.totalPages;
    
    // Update page numbers
    pageNumbers.innerHTML = '';
    const startPage = Math.max(1, tablesPagination.currentPage - 2);
    const endPage = Math.min(tablesPagination.totalPages, tablesPagination.currentPage + 2);
    
    // Add ellipsis if needed
    if (startPage > 1) {
        const firstBtn = document.createElement('button');
        firstBtn.className = 'pagination-number';
        firstBtn.textContent = '1';
        firstBtn.onclick = () => goToTablesPage(1);
        pageNumbers.appendChild(firstBtn);
        
        if (startPage > 2) {
            const ellipsis = document.createElement('span');
            ellipsis.className = 'pagination-ellipsis';
            ellipsis.textContent = '...';
            pageNumbers.appendChild(ellipsis);
        }
    }
    
    // Add page numbers
    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.className = `pagination-number ${i === tablesPagination.currentPage ? 'active' : ''}`;
        pageBtn.textContent = i;
        pageBtn.onclick = () => goToTablesPage(i);
        pageNumbers.appendChild(pageBtn);
    }
    
    // Add ellipsis if needed
    if (endPage < tablesPagination.totalPages) {
        if (endPage < tablesPagination.totalPages - 1) {
            const ellipsis = document.createElement('span');
            ellipsis.className = 'pagination-ellipsis';
            ellipsis.textContent = '...';
            pageNumbers.appendChild(ellipsis);
        }
        
        const lastBtn = document.createElement('button');
        lastBtn.className = 'pagination-number';
        lastBtn.textContent = tablesPagination.totalPages;
        lastBtn.onclick = () => goToTablesPage(tablesPagination.totalPages);
        pageNumbers.appendChild(lastBtn);
    }
}

// Show specific rooms page
function showRoomsPage(page) {
    console.log(`📄 showRoomsPage called with page: ${page}`);
    
    const roomsContainer = document.querySelector('.room-table-container');
    const roomsEmptyState = roomsContainer?.querySelector('.empty-state');
    const isRoomsEmptyStateVisible = roomsEmptyState && roomsEmptyState.style.display !== 'none';
    
    if (isRoomsEmptyStateVisible) {
        console.log('📄 Rooms in empty state, skipping page show');
        return;
    }
    
    const roomsTableBody = roomsContainer ? roomsContainer.querySelector('tbody') : null;
    if (!roomsTableBody) {
        console.log('📄 Rooms table body not found, likely in empty state');
        return;
    }
    
    const allRows = roomsTableBody.querySelectorAll('tr');
    const startIndex = (page - 1) * roomsPagination.itemsPerPage;
    const endIndex = startIndex + roomsPagination.itemsPerPage;
    
    console.log(`📄 Showing rooms page ${page}:`, {
        totalRows: allRows.length,
        itemsPerPage: roomsPagination.itemsPerPage,
        startIndex,
        endIndex,
        totalPages: roomsPagination.totalPages,
        currentPage: roomsPagination.currentPage
    });
    
    // Debug: Log all rows before hiding
    console.log('📄 All rows before hiding:', Array.from(allRows).map((row, index) => ({
        index: index + 1,
        roomId: row.getAttribute('data-room-id'),
        roomName: row.querySelector('.room-name') ? row.querySelector('.room-name').textContent.trim() : 'No name',
        currentDisplay: row.style.display
    })));
    
    // Hide all rows using class
    allRows.forEach((row, index) => {
        row.classList.add('pagination-hidden');
        console.log(`📄 Hidden row ${index + 1} (${row.querySelector('.room-name')?.textContent.trim() || 'No name'})`);
    });
    
    // Show rows for current page - ensure we show up to itemsPerPage rows
    const maxRowsToShow = Math.min(roomsPagination.itemsPerPage, allRows.length - startIndex);
    const actualEndIndex = startIndex + maxRowsToShow;
    
    console.log(`📄 Showing ${maxRowsToShow} rows from index ${startIndex} to ${actualEndIndex - 1}`);
    console.log(`📄 Available rows: ${allRows.length}, Start: ${startIndex}, End: ${actualEndIndex}`);
    
    for (let i = startIndex; i < actualEndIndex && i < allRows.length; i++) {
        allRows[i].classList.remove('pagination-hidden');
        const roomName = allRows[i].querySelector('.room-name')?.textContent.trim() || 'No name';
        console.log(`📄 Shown row ${i + 1}: ${roomName}`);
    }
    
    roomsPagination.currentPage = page;
    updateRoomsPagination();
    
    // Debug: Log visible rows after showing
    const visibleRows = Array.from(allRows).filter(row => row.style.display !== 'none');
    console.log('📄 Visible rows after showing:', visibleRows.map((row, index) => ({
        index: index + 1,
        roomId: row.getAttribute('data-room-id'),
        roomName: row.querySelector('.room-name') ? row.querySelector('.room-name').textContent.trim() : 'No name'
    })));
    
    console.log(`📄 Showing rooms page ${page}: rows ${startIndex + 1}-${Math.min(endIndex, allRows.length)}`);
}

// Show specific tables page
function showTablesPage(page) {
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    const tablesEmptyState = tablesContainer?.querySelector('.empty-state');
    const isTablesEmptyStateVisible = tablesEmptyState && tablesEmptyState.style.display !== 'none';
    
    if (isTablesEmptyStateVisible) {
        console.log('📄 Tables in empty state, skipping page show');
        return;
    }
    
    const tablesTableBody = tablesContainer ? tablesContainer.querySelector('tbody') : null;
    if (!tablesTableBody) {
        console.log('📄 Tables table body not found, likely in empty state');
        return;
    }
    
    const allRows = tablesTableBody.querySelectorAll('tr');
    const startIndex = (page - 1) * tablesPagination.itemsPerPage;
    const endIndex = startIndex + tablesPagination.itemsPerPage;
    
    // Hide all rows using class
    allRows.forEach(row => {
        row.classList.add('pagination-hidden');
    });
    
    // Show rows for current page
    for (let i = startIndex; i < endIndex && i < allRows.length; i++) {
        allRows[i].classList.remove('pagination-hidden');
    }
    
    tablesPagination.currentPage = page;
    updateTablesPagination();
    
    console.log(`📄 Showing tables page ${page}: rows ${startIndex + 1}-${Math.min(endIndex, allRows.length)}`);
}

// Change rooms page by offset
function changeRoomsPage(offset) {
    const newPage = roomsPagination.currentPage + offset;
    if (newPage >= 1 && newPage <= roomsPagination.totalPages) {
        showRoomsPage(newPage);
    }
}

// Change tables page by offset
function changeTablesPage(offset) {
    const newPage = tablesPagination.currentPage + offset;
    if (newPage >= 1 && newPage <= tablesPagination.totalPages) {
        showTablesPage(newPage);
    }
}

// Go to specific rooms page
function goToRoomsPage(page) {
    if (page >= 1 && page <= roomsPagination.totalPages) {
        showRoomsPage(page);
    }
}

// Go to specific tables page
function goToTablesPage(page) {
    if (page >= 1 && page <= tablesPagination.totalPages) {
        showTablesPage(page);
    }
}

// Smart pagination refresh after deletion (updated to handle empty state)
function refreshPaginationAfterDeletion() {
    console.log('🔄 Smart pagination refresh after deletion...');
    
    // Check rooms pagination
    const roomsContainer = document.querySelector('.room-table-container');
    const roomsEmptyState = roomsContainer?.querySelector('.empty-state');
    const isRoomsEmptyStateVisible = roomsEmptyState && roomsEmptyState.style.display !== 'none';
    
    if (isRoomsEmptyStateVisible) {
        console.log('🏠 Rooms in empty state, skipping pagination refresh');
    } else {
        // Recalculate rooms pagination
        const roomsTableBody = roomsContainer ? roomsContainer.querySelector('tbody') : null;
        if (roomsTableBody) {
            const roomsRows = roomsTableBody.querySelectorAll('tr');
            const newTotalItems = roomsRows.length;
            const newTotalPages = Math.ceil(newTotalItems / roomsPagination.itemsPerPage);
            
            console.log('📊 Pagination after deletion (Rooms):', {
                oldTotalItems: roomsPagination.totalItems,
                newTotalItems: newTotalItems,
                oldTotalPages: roomsPagination.totalPages,
                newTotalPages: newTotalPages,
                currentPage: roomsPagination.currentPage
            });
            
            roomsPagination.totalItems = newTotalItems;
            roomsPagination.totalPages = newTotalPages;
            
            // Adjust current page if needed
            if (roomsPagination.currentPage > roomsPagination.totalPages && roomsPagination.totalPages > 0) {
                roomsPagination.currentPage = roomsPagination.totalPages;
            }
            
            updateRoomsPagination();
            showRoomsPage(roomsPagination.currentPage);
        } else {
            console.log('🏠 Rooms table body not found, likely in empty state');
        }
    }
    
    // Check tables pagination
    const tablesContainer2 = document.querySelectorAll('.room-table-container')[1];
    const tablesEmptyState = tablesContainer2?.querySelector('.empty-state');
    const isTablesEmptyStateVisible = tablesEmptyState && tablesEmptyState.style.display !== 'none';
    
    if (isTablesEmptyStateVisible) {
        console.log('🪑 Tables in empty state, skipping pagination refresh');
    } else {
        // Recalculate tables pagination
        const tablesTableBody = tablesContainer2 ? tablesContainer2.querySelector('tbody') : null;
        if (tablesTableBody) {
            const tablesRows = tablesTableBody.querySelectorAll('tr');
            const newTotalItems = tablesRows.length;
            const newTotalPages = Math.ceil(newTotalItems / tablesPagination.itemsPerPage);
            
            console.log('📊 Pagination after deletion (Tables):', {
                oldTotalItems: tablesPagination.totalItems,
                newTotalItems: newTotalItems,
                oldTotalPages: tablesPagination.totalPages,
                newTotalPages: newTotalPages,
                currentPage: tablesPagination.currentPage
            });
            
            tablesPagination.totalItems = newTotalItems;
            tablesPagination.totalPages = newTotalPages;
            
            // Adjust current page if needed
            if (tablesPagination.currentPage > tablesPagination.totalPages && tablesPagination.totalPages > 0) {
                tablesPagination.currentPage = tablesPagination.totalPages;
            }
            
            updateTablesPagination();
            showTablesPage(tablesPagination.currentPage);
        } else {
            console.log('🪑 Tables table body not found, likely in empty state');
        }
    }
}

// Refresh pagination after data changes
function refreshPagination(forceGoToPage1 = false, isNewItemAdded = false) {
    console.log('🔄 Refreshing pagination...', { forceGoToPage1, isNewItemAdded });
    
    // Recalculate rooms pagination
    const roomsContainer = document.querySelector('.room-table-container');
    const roomsTableBody = roomsContainer ? roomsContainer.querySelector('tbody') : null;
    if (roomsTableBody) {
        const roomsRows = roomsTableBody.querySelectorAll('tr');
        roomsPagination.totalItems = roomsRows.length;
        roomsPagination.totalPages = Math.ceil(roomsPagination.totalItems / roomsPagination.itemsPerPage);
        
        if (forceGoToPage1) {
            roomsPagination.currentPage = 1;
        }
        
        updateRoomsPagination();
        showRoomsPage(roomsPagination.currentPage);
    }
    
    // Recalculate tables pagination
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    const tablesTableBody = tablesContainer ? tablesContainer.querySelector('tbody') : null;
    if (tablesTableBody) {
        const tablesRows = tablesTableBody.querySelectorAll('tr');
        tablesPagination.totalItems = tablesRows.length;
        tablesPagination.totalPages = Math.ceil(tablesPagination.totalItems / tablesPagination.itemsPerPage);
        
        if (forceGoToPage1) {
            tablesPagination.currentPage = 1;
        }
        
        updateTablesPagination();
        showTablesPage(tablesPagination.currentPage);
    }
}

// Force refresh pagination (used when data changes significantly)
function forceRefreshPagination() {
    console.log('🚨 Force refreshing pagination...');
    
    // Force refresh rooms pagination
    const roomsContainer = document.querySelector('.room-table-container');
    const roomsTableBody = roomsContainer ? roomsContainer.querySelector('tbody') : null;
    if (roomsTableBody) {
        const roomsRows = roomsTableBody.querySelectorAll('tr');
        roomsPagination.totalItems = roomsRows.length;
        roomsPagination.totalPages = Math.ceil(roomsPagination.totalItems / roomsPagination.itemsPerPage);
        roomsPagination.currentPage = 1;
        
        updateRoomsPagination();
        showRoomsPage(1);
    }
    
    // Force refresh tables pagination
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    const tablesTableBody = tablesContainer ? tablesContainer.querySelector('tbody') : null;
    if (tablesTableBody) {
        const tablesRows = tablesTableBody.querySelectorAll('tr');
        tablesPagination.totalItems = tablesRows.length;
        tablesPagination.totalPages = Math.ceil(tablesPagination.totalItems / tablesPagination.itemsPerPage);
        tablesPagination.currentPage = 1;
        
        updateTablesPagination();
        showTablesPage(1);
    }
}

// Initialize Enhanced Room Table Manager
function initializeEnhancedRoomTableManager() {
    console.log('🚀 Initializing Enhanced Room Table Manager');
    
    // Initialize pagination
    initializePagination();
    
    // Update room dropdown
    updateAllRoomOptions();
    
    // Update capacity badges
    updateCapacityBadges();
    
    console.log('✅ Enhanced Room Table Manager initialized');
}

// Auto-initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    initializeEnhancedRoomTableManager();
});

// Export functions for global access
window.initializeEnhancedRoomTableManager = initializeEnhancedRoomTableManager;
window.forceRefreshPagination = forceRefreshPagination;

// Refresh pagination after data changes
function refreshPagination(forceGoToPage1 = false, isNewItemAdded = false) {
    console.log('🔄 Refreshing pagination...', { forceGoToPage1, isNewItemAdded });
    
    // Recalculate rooms pagination
    const roomsContainer = document.querySelector('.room-table-container');
    const roomsTableBody = roomsContainer ? roomsContainer.querySelector('tbody') : null;
    if (roomsTableBody) {
        const roomsRows = roomsTableBody.querySelectorAll('tr');
        roomsPagination.totalItems = roomsRows.length;
        roomsPagination.totalPages = Math.ceil(roomsPagination.totalItems / roomsPagination.itemsPerPage);
        
        // Always go to page 1 when new items are added
        if (isNewItemAdded || forceGoToPage1) {
            roomsPagination.currentPage = 1;
            console.log('📄 Rooms: Going to page 1 to show new item');
        } else if (roomsPagination.currentPage > roomsPagination.totalPages && roomsPagination.totalPages > 0) {
            // Adjust current page if it's beyond total pages
            roomsPagination.currentPage = roomsPagination.totalPages;
        }
        
        updateRoomsPagination();
        showRoomsPage(roomsPagination.currentPage);
    }
    
    // Recalculate tables pagination
    const tablesContainer = document.querySelectorAll('.room-table-container')[1];
    const tablesTableBody = tablesContainer ? tablesContainer.querySelector('tbody') : null;
    if (tablesTableBody) {
        const tablesRows = tablesTableBody.querySelectorAll('tr');
        tablesPagination.totalItems = tablesRows.length;
        tablesPagination.totalPages = Math.ceil(tablesPagination.totalItems / tablesPagination.itemsPerPage);
        
        // Always go to page 1 when new items are added
        if (isNewItemAdded || forceGoToPage1) {
            tablesPagination.currentPage = 1;
            console.log('📄 Tables: Going to page 1 to show new item');
        } else if (tablesPagination.currentPage > tablesPagination.totalPages && tablesPagination.totalPages > 0) {
            // Adjust current page if it's beyond total pages
            tablesPagination.currentPage = tablesPagination.totalPages;
        }
        
        updateTablesPagination();
        showTablesPage(tablesPagination.currentPage);
    }
    
    console.log('✅ Pagination refreshed:', { roomsPagination, tablesPagination });
}

// Make pagination functions global
window.changeRoomsPage = changeRoomsPage;
window.changeTablesPage = changeTablesPage;
window.goToRoomsPage = goToRoomsPage;
window.goToTablesPage = goToTablesPage;
window.refreshPagination = refreshPagination;


// Force rooms pagination specifically
window.forceRoomsPagination = function() {
    console.log('🚨 Force rooms pagination...');
    
    const roomsTableBody = document.querySelector('.room-table-container:first-of-type tbody');
    if (roomsTableBody) {
        const rows = roomsTableBody.querySelectorAll('tr');
        console.log(`🚨 Rooms table: Found ${rows.length} rows`);
        
        rows.forEach((row, index) => {
            if (index >= 5) {
                row.classList.add('pagination-hidden');
                console.log(`🚨 Hidden rooms row ${index + 1}`);
            } else {
                row.classList.remove('pagination-hidden');
                console.log(`🚨 Shown rooms row ${index + 1}`);
            }
        });
        
        // Update pagination state
        roomsPagination.totalItems = rows.length;
        roomsPagination.totalPages = Math.ceil(roomsPagination.totalItems / roomsPagination.itemsPerPage);
        roomsPagination.currentPage = 1;
        updateRoomsPagination();
        
        console.log('🚨 Rooms pagination forced:', roomsPagination);
    } else {
        console.error('🚨 Rooms table body not found!');
    }
};

// Function to change rooms page size
window.changeRoomsPageSize = function(newSize) {
    console.log('🔄 Changing rooms page size to:', newSize);
    
    const newItemsPerPage = parseInt(newSize);
    if (newItemsPerPage !== roomsPagination.itemsPerPage) {
        // Calculate new current page to maintain relative position
        const currentItemIndex = (roomsPagination.currentPage - 1) * roomsPagination.itemsPerPage;
        const newCurrentPage = Math.max(1, Math.floor(currentItemIndex / newItemsPerPage) + 1);
        
        console.log('📊 Page size change calculation:', {
            oldItemsPerPage: roomsPagination.itemsPerPage,
            newItemsPerPage: newItemsPerPage,
            currentItemIndex: currentItemIndex,
            oldCurrentPage: roomsPagination.currentPage,
            newCurrentPage: newCurrentPage
        });
        
        // Update pagination settings
        roomsPagination.itemsPerPage = newItemsPerPage;
        roomsPagination.currentPage = newCurrentPage;
        
        // Recalculate total pages
        const roomsContainer = document.querySelector('.room-table-container');
        const roomsTableBody = roomsContainer ? roomsContainer.querySelector('tbody') : null;
        if (roomsTableBody) {
            const roomsRows = roomsTableBody.querySelectorAll('tr');
            roomsPagination.totalItems = roomsRows.length;
            roomsPagination.totalPages = Math.ceil(roomsPagination.totalItems / roomsPagination.itemsPerPage);
            
            // Ensure current page is within bounds
            if (roomsPagination.currentPage > roomsPagination.totalPages && roomsPagination.totalPages > 0) {
                roomsPagination.currentPage = roomsPagination.totalPages;
            }
            
            // Update UI
            updateRoomsPagination();
            showRoomsPage(roomsPagination.currentPage);
            
            // Auto scroll to rooms section title
            const roomsSectionTitle = document.querySelector('.section-title');
            if (roomsSectionTitle) {
                roomsSectionTitle.scrollIntoView({ 
                    behavior: 'smooth', 
                    block: 'start' 
                });
                console.log('📜 Scrolled to rooms section title');
            }
            
            console.log('✅ Rooms page size changed successfully');
        }
    }
};

// Function to change tables page size
window.changeTablesPageSize = function(newSize) {
    console.log('🔄 Changing tables page size to:', newSize);
    
    const newItemsPerPage = parseInt(newSize);
    if (newItemsPerPage !== tablesPagination.itemsPerPage) {
        // Calculate new current page to maintain relative position
        const currentItemIndex = (tablesPagination.currentPage - 1) * tablesPagination.itemsPerPage;
        const newCurrentPage = Math.max(1, Math.floor(currentItemIndex / newItemsPerPage) + 1);
        
        console.log('📊 Page size change calculation:', {
            oldItemsPerPage: tablesPagination.itemsPerPage,
            newItemsPerPage: newItemsPerPage,
            currentItemIndex: currentItemIndex,
            oldCurrentPage: tablesPagination.currentPage,
            newCurrentPage: newCurrentPage
        });
        
        // Update pagination settings
        tablesPagination.itemsPerPage = newItemsPerPage;
        tablesPagination.currentPage = newCurrentPage;
        
        // Recalculate total pages
        const tablesContainer = document.querySelectorAll('.room-table-container')[1];
        const tablesTableBody = tablesContainer ? tablesContainer.querySelector('tbody') : null;
        if (tablesTableBody) {
            const tablesRows = tablesTableBody.querySelectorAll('tr');
            tablesPagination.totalItems = tablesRows.length;
            tablesPagination.totalPages = Math.ceil(tablesPagination.totalItems / tablesPagination.itemsPerPage);
            
            // Ensure current page is within bounds
            if (tablesPagination.currentPage > tablesPagination.totalPages && tablesPagination.totalPages > 0) {
                tablesPagination.currentPage = tablesPagination.totalPages;
            }
            
            // Update UI
            updateTablesPagination();
            showTablesPage(tablesPagination.currentPage);
            
            // Auto scroll to tables section title
            const sectionTitles = document.querySelectorAll('.section-title');
            const tablesSectionTitle = sectionTitles[1]; // Second section title is for tables
            if (tablesSectionTitle) {
                tablesSectionTitle.scrollIntoView({ 
                    behavior: 'smooth', 
                    block: 'start' 
                });
                console.log('📜 Scrolled to tables section title');
            }
            
            console.log('✅ Tables page size changed successfully');
        }
    }
};

// Export for global access
window.RoomTableManager = RoomTableManager;

// ============================================================
// IMPORT/EXPORT EXCEL FUNCTIONALITY
// ============================================================

let selectedFile = null;

// Show Import Modal
window.showImportModal = function() {
    const modal = document.getElementById('importExcelModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
        
        // Reset modal state
        resetImportModal();
        
        // Setup drag and drop
        setupDragAndDrop();
        
        // Add click outside handler specifically for this modal
        modal.onclick = function(event) {
            if (event.target === modal) {
                closeImportModal();
            }
        };
    }
};

// Close Import Modal
window.closeImportModal = function() {
    const modal = document.getElementById('importExcelModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        
        // Remove click outside handler
        modal.onclick = null;
        
        resetImportModal();
    }
};

// Reset Import Modal
function resetImportModal() {
    selectedFile = null;
    
    // Reset file input
    const fileInput = document.getElementById('excelFile');
    if (fileInput) {
        fileInput.value = '';
    }
    
    // Hide file preview
    const filePreview = document.getElementById('filePreview');
    const fileUploadArea = document.getElementById('fileUploadArea');
    if (filePreview) filePreview.style.display = 'none';
    if (fileUploadArea) fileUploadArea.style.display = 'block';
    
    // Hide progress and results
    const progress = document.getElementById('importProgress');
    const results = document.getElementById('importResults');
    if (progress) progress.style.display = 'none';
    if (results) results.style.display = 'none';
    
    // Reset buttons
    const checkBtn = document.getElementById('checkBtn');
    const importBtn = document.getElementById('importBtn');
    if (checkBtn) {
        checkBtn.disabled = true;
        checkBtn.style.display = 'inline-block';
    }
    if (importBtn) {
        importBtn.disabled = true;
        importBtn.style.display = 'none';
    }
}

// Setup Drag and Drop
function setupDragAndDrop() {
    const uploadArea = document.getElementById('fileUploadArea');
    if (!uploadArea) return;

    // Prevent default drag behaviors
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        uploadArea.addEventListener(eventName, preventDefaults, false);
        document.body.addEventListener(eventName, preventDefaults, false);
    });

    // Highlight drop area when item is dragged over it
    ['dragenter', 'dragover'].forEach(eventName => {
        uploadArea.addEventListener(eventName, highlight, false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
        uploadArea.addEventListener(eventName, unhighlight, false);
    });

    // Handle dropped files
    uploadArea.addEventListener('drop', handleDrop, false);
}

function preventDefaults(e) {
    e.preventDefault();
    e.stopPropagation();
}

function highlight(e) {
    const uploadArea = document.getElementById('fileUploadArea');
    uploadArea.classList.add('dragover');
}

function unhighlight(e) {
    const uploadArea = document.getElementById('fileUploadArea');
    uploadArea.classList.remove('dragover');
}

function handleDrop(e) {
    const dt = e.dataTransfer;
    const files = dt.files;
    
    if (files.length > 0) {
        handleFileSelect({ target: { files: files } });
    }
}

// Handle File Selection
window.handleFileSelect = function(event) {
    const file = event.target.files[0];
    if (!file) return;

    // Validate file type
    const allowedTypes = [
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', // .xlsx
        'application/vnd.ms-excel' // .xls
    ];
    
    if (!allowedTypes.includes(file.type) && !file.name.match(/\.(xlsx|xls)$/i)) {
        showNotification('error', 'Vui lòng chọn file Excel (.xlsx hoặc .xls)', 'Định dạng file không hợp lệ');
        return;
    }

    // Validate file size (max 10MB)
    const maxSize = 10 * 1024 * 1024; // 10MB
    if (file.size > maxSize) {
        showNotification('error', 'File quá lớn', 'Kích thước file không được vượt quá 10MB');
        return;
    }

    selectedFile = file;
    showFilePreview(file);
    
    // Enable check button and reset import button
    const checkBtn = document.getElementById('checkBtn');
    const importBtn = document.getElementById('importBtn');
    if (checkBtn) {
        checkBtn.disabled = false;
        checkBtn.style.display = 'inline-block';
    }
    if (importBtn) {
        importBtn.disabled = true;
        importBtn.style.display = 'none';
    }
};

// Show File Preview
function showFilePreview(file) {
    const filePreview = document.getElementById('filePreview');
    const fileUploadArea = document.getElementById('fileUploadArea');
    const fileName = document.getElementById('fileName');
    const fileSize = document.getElementById('fileSize');

    if (filePreview && fileUploadArea && fileName && fileSize) {
        fileName.textContent = file.name;
        fileSize.textContent = formatFileSize(file.size);
        
        fileUploadArea.style.display = 'none';
        filePreview.style.display = 'block';
    }
}

// Format File Size
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Remove File
window.removeFile = function() {
    selectedFile = null;
    const fileInput = document.getElementById('excelFile');
    if (fileInput) {
        fileInput.value = '';
    }
    
    const filePreview = document.getElementById('filePreview');
    const fileUploadArea = document.getElementById('fileUploadArea');
    if (filePreview) filePreview.style.display = 'none';
    if (fileUploadArea) fileUploadArea.style.display = 'block';
    
    const importBtn = document.getElementById('importBtn');
    if (importBtn) {
        importBtn.disabled = true;
    }
};

// Check File
window.checkFile = function() {
    if (!selectedFile) {
        showNotification('error', 'Chưa chọn file', 'Vui lòng chọn file Excel để kiểm tra');
        return;
    }
    
    const formData = new FormData();
    formData.append('excelFile', selectedFile);
    formData.append('action', 'checkExcel');
    
    // Get import options
    const skipDuplicates = document.getElementById('skipDuplicates').checked;
    const validateData = document.getElementById('validateData').checked;
    const createMissingRooms = document.getElementById('createMissingRooms').checked;
    
    formData.append('skipDuplicates', skipDuplicates);
    formData.append('validateData', validateData);
    formData.append('createMissingRooms', createMissingRooms);
    
    // Show progress
    showProgress();
    
    fetch('roomtable', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        hideProgress();
        
        if (data.success) {
            // Show check results
            showCheckResults(data);
            
            // Enable import button if no errors
            if (data.errors && data.errors.length === 0) {
                const checkBtn = document.getElementById('checkBtn');
                const importBtn = document.getElementById('importBtn');
                if (checkBtn) {
                    checkBtn.style.display = 'none';
                }
                if (importBtn) {
                    importBtn.disabled = false;
                    importBtn.style.display = 'inline-block';
                }
            }
        } else {
            showNotification('error', 'Lỗi kiểm tra file', data.message || 'Có lỗi xảy ra khi kiểm tra file');
        }
    })
    .catch(error => {
        hideProgress();
        console.error('Error checking file:', error);
        showNotification('error', 'Lỗi kiểm tra file', 'Có lỗi xảy ra khi kiểm tra file');
    });
};

// Show Check Results
function showCheckResults(data) {
    const importResults = document.getElementById('importResults');
    const resultSummary = document.getElementById('resultSummary');
    const resultDetails = document.getElementById('resultDetails');
    
    if (!importResults || !resultSummary || !resultDetails) return;
    
    // Show results section
    importResults.style.display = 'block';
    
    // Summary
    let summaryHtml = `
        <div class="result-item">
            <div class="result-number">${data.totalRooms || 0}</div>
            <div class="result-label">Phòng</div>
        </div>
        <div class="result-item">
            <div class="result-number">${data.totalTables || 0}</div>
            <div class="result-label">Bàn</div>
        </div>
        <div class="result-item">
            <div class="result-number">${data.errors ? data.errors.length : 0}</div>
            <div class="result-label">Lỗi</div>
        </div>
    `;
    resultSummary.innerHTML = summaryHtml;
    
    // Details
    if (data.errors && data.errors.length > 0) {
        let detailsHtml = '<h5>Danh sách lỗi:</h5><ul>';
        data.errors.forEach(error => {
            detailsHtml += `<li class="error-item">${error}</li>`;
        });
        detailsHtml += '</ul>';
        resultDetails.innerHTML = detailsHtml;
    } else {
        resultDetails.innerHTML = '<div class="success-message">✅ File hợp lệ, có thể bắt đầu nhập dữ liệu!</div>';
    }
}

// Start Import Process
window.startImport = function() {
    if (!selectedFile) {
        showNotification('error', 'Vui lòng chọn file Excel', 'Chưa có file nào được chọn');
        return;
    }

    // Show progress
    const progress = document.getElementById('importProgress');
    const results = document.getElementById('importResults');
    const importBtn = document.getElementById('importBtn');
    
    if (progress) progress.style.display = 'block';
    if (results) results.style.display = 'none';
    if (importBtn) {
        importBtn.disabled = true;
        importBtn.textContent = '⏳ Đang xử lý...';
    }

    // Create FormData
    const formData = new FormData();
    formData.append('file', selectedFile);
    formData.append('action', 'importExcel');
    formData.append('skipDuplicates', document.getElementById('skipDuplicates').checked);
    formData.append('validateData', document.getElementById('validateData').checked);
    formData.append('createMissingRooms', document.getElementById('createMissingRooms').checked);

    // Simulate progress
    simulateProgress();

    // Send request
    fetch('roomtable', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        hideProgress();
        showImportResults(data);
        
        if (importBtn) {
            importBtn.disabled = false;
            importBtn.textContent = '✅ Hoàn thành';
        }
    })
    .catch(error => {
        console.error('Import error:', error);
        hideProgress();
        showNotification('error', 'Lỗi khi nhập dữ liệu', error.message);
        
        if (importBtn) {
            importBtn.disabled = false;
            importBtn.textContent = '✅ Bắt đầu nhập';
        }
    });
};

// Simulate Progress
function simulateProgress() {
    const progressFill = document.getElementById('progressFill');
    const progressText = document.getElementById('progressText');
    
    let progress = 0;
    const interval = setInterval(() => {
        progress += Math.random() * 15;
        if (progress > 90) progress = 90;
        
        if (progressFill) {
            progressFill.style.width = progress + '%';
        }
        
        if (progressText) {
            if (progress < 30) {
                progressText.textContent = 'Đang đọc file Excel...';
            } else if (progress < 60) {
                progressText.textContent = 'Đang kiểm tra dữ liệu...';
            } else if (progress < 90) {
                progressText.textContent = 'Đang lưu vào cơ sở dữ liệu...';
            } else {
                progressText.textContent = 'Hoàn thành!';
            }
        }
    }, 200);

    // Store interval ID for cleanup
    window.importProgressInterval = interval;
}

// Show Progress
function showProgress() {
    const progressDiv = document.getElementById('importProgress');
    if (progressDiv) {
        progressDiv.style.display = 'block';
    }
    
    // Reset progress bar
    const progressFill = document.getElementById('progressFill');
    const progressText = document.getElementById('progressText');
    if (progressFill) {
        progressFill.style.width = '0%';
    }
    if (progressText) {
        progressText.textContent = 'Đang kiểm tra file...';
    }
    
    // Simulate progress
    simulateProgress();
}

// Hide Progress
function hideProgress() {
    if (window.importProgressInterval) {
        clearInterval(window.importProgressInterval);
        window.importProgressInterval = null;
    }
    
    const progressFill = document.getElementById('progressFill');
    if (progressFill) {
        progressFill.style.width = '100%';
    }
}

// Show Import Results
function showImportResults(data) {
    const results = document.getElementById('importResults');
    const summary = document.getElementById('resultSummary');
    const details = document.getElementById('resultDetails');
    
    if (!results || !summary || !details) return;

    // Build summary
    let summaryHtml = '';
    if (data.rooms) {
        summaryHtml += `
            <div class="result-item success">
                <div class="result-number">${data.rooms.success || 0}</div>
                <div class="result-label">Phòng thành công</div>
            </div>
            <div class="result-item error">
                <div class="result-number">${data.rooms.error || 0}</div>
                <div class="result-label">Phòng lỗi</div>
            </div>
        `;
    }
    
    if (data.tables) {
        summaryHtml += `
            <div class="result-item success">
                <div class="result-number">${data.tables.success || 0}</div>
                <div class="result-label">Bàn thành công</div>
            </div>
            <div class="result-item error">
                <div class="result-number">${data.tables.error || 0}</div>
                <div class="result-label">Bàn lỗi</div>
            </div>
        `;
    }

    summary.innerHTML = summaryHtml;

    // Build details
    let detailsHtml = '';
    if (data.message) {
        detailsHtml += `<p><strong>Thông báo:</strong> ${data.message}</p>`;
    }
    
    if (data.errors && data.errors.length > 0) {
        detailsHtml += '<p><strong>Chi tiết lỗi:</strong></p><ul>';
        data.errors.forEach(error => {
            detailsHtml += `<li>${error}</li>`;
        });
        detailsHtml += '</ul>';
    }

    details.innerHTML = detailsHtml;
    results.style.display = 'block';

    // Show notification
    if (data.success) {
        showNotification('success', 'Nhập dữ liệu thành công', data.message);
        // Refresh the page to show new data
        setTimeout(() => {
            window.location.reload();
        }, 2000);
    } else {
        showNotification('warning', 'Nhập dữ liệu hoàn thành với lỗi', data.message);
    }
};

// Export to Excel
window.exportToExcel = function() {
    // First check if there's data to export
    const tbody = document.querySelector('.table tbody');
    const rows = tbody ? tbody.querySelectorAll('tr[data-table-id]') : [];
    
    if (rows.length === 0) {
        showNotification('error', 'Không có dữ liệu để xuất Excel');
        return;
    }
    
    showNotification('info', 'Đang xuất dữ liệu...', 'Vui lòng chờ trong giây lát');
    
    // Use fetch to check response
    fetch('roomtable', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
            action: 'exportExcel'
        })
    })
    .then(response => {
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return response.json().then(data => {
                if (!data.success) {
                    showNotification('error', data.message || 'Không có dữ liệu để xuất');
                }
            });
        } else {
            // It's a file download
            return response.blob().then(blob => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = 'danh_sach_phong_ban_' + new Date().toISOString().split('T')[0] + '.xlsx';
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
                showNotification('success', 'Xuất Excel thành công');
            });
        }
    })
    .catch(error => {
        console.error('Export error:', error);
        showNotification('error', 'Lỗi khi xuất Excel');
    });
};

// Download Template
window.downloadTemplate = function(type) {
    showNotification('info', 'Đang tải template...', 'Vui lòng chờ trong giây lát');
    
    // Create a form to submit the template download request
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = 'roomtable';
    form.style.display = 'none';
    
    const actionInput = document.createElement('input');
    actionInput.type = 'hidden';
    actionInput.name = 'action';
    actionInput.value = 'downloadTemplate';
    
    const typeInput = document.createElement('input');
    typeInput.type = 'hidden';
    typeInput.name = 'templateType';
    typeInput.value = type;
    
    form.appendChild(actionInput);
    form.appendChild(typeInput);
    document.body.appendChild(form);
    form.submit();
    document.body.removeChild(form);
};

// Enhanced Notification System
function showNotification(type, title, message) {
    // Remove existing notifications
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notification => notification.remove());

    // Create notification
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    
    const icon = getNotificationIcon(type);
    notification.innerHTML = `
        <div class="notification-icon">${icon}</div>
        <div class="notification-content">
            <div class="notification-title">${title}</div>
            <div class="notification-message">${message}</div>
        </div>
        <div class="notification-close" onclick="this.parentElement.remove()">×</div>
        <div class="notification-progress"></div>
    `;

    // Add to page
    document.body.appendChild(notification);

    // Show notification
    setTimeout(() => {
        notification.classList.add('show');
    }, 100);

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentElement) {
            notification.classList.remove('show');
            setTimeout(() => {
                if (notification.parentElement) {
                    notification.remove();
                }
            }, 300);
        }
    }, 5000);
}

function getNotificationIcon(type) {
    const icons = {
        success: '✅',
        error: '❌',
        warning: '⚠️',
        info: 'ℹ️'
    };
    return icons[type] || 'ℹ️';
}

// Update modal click outside handler
window.onclick = function (event) {
    const roomModal = document.getElementById('addRoomModal');
    const tableModal = document.getElementById('addTableModal');
    const importModal = document.getElementById('importExcelModal');
    
    // Close room modal when clicking outside
    if (event.target === roomModal) {
        closeAddRoomModal();
    }
    
    // Close table modal when clicking outside
    if (event.target === tableModal) {
        closeAddTableModal();
    }
    
    // Close import modal when clicking outside
    if (event.target === importModal) {
        closeImportModal();
    }
}

// Alternative approach: Add event listener to document for better compatibility
document.addEventListener('click', function(event) {
    const roomModal = document.getElementById('addRoomModal');
    const tableModal = document.getElementById('addTableModal');
    const importModal = document.getElementById('importExcelModal');
    
    // Check if click is outside modal content
    if (roomModal && roomModal.style.display === 'block') {
        if (event.target === roomModal) {
            closeAddRoomModal();
        }
    }
    
    if (tableModal && tableModal.style.display === 'block') {
        if (event.target === tableModal) {
            closeAddTableModal();
        }
    }
    
    if (importModal && importModal.style.display === 'block') {
        if (event.target === importModal) {
            closeImportModal();
        }
    }
});
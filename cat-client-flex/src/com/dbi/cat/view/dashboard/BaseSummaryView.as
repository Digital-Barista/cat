package com.dbi.cat.view.dashboard
{
    import com.dbi.cat.common.constants.EntryPointType;
    import com.dbi.cat.common.vo.ClientVO;
    import com.dbi.cat.common.vo.DashboardCountVO;
    import com.dbi.cat.common.vo.DashboardDataVO;
    import com.dbi.cat.common.vo.MessageCreditInfoVO;
    import com.dbi.cat.common.vo.UserVO;
    
    import mx.collections.ArrayCollection;
    import mx.containers.Canvas;
    
    public class BaseSummaryView extends Canvas
    {
        public function BaseSummaryView()
        {
            super();
        }
        
        
        private var _currentUser:UserVO;
        public function get currentUser():UserVO
        {
            return _currentUser;
        }
        public function set currentUser(value:UserVO):void
        {
            _currentUser = value;
        }
        
        
        private var _dashboardData:DashboardDataVO;
        [Bindable]
        public function get dashboardData():DashboardDataVO
        {
            return _dashboardData;
        }
        public function set dashboardData(value:DashboardDataVO):void
        {
            _dashboardData = value;
            dispatchEvent(new Event("dashboardDataUpdate"));
        }
        
        protected var _filterClients:ArrayCollection;
        public function set filterClients(value:ArrayCollection):void
        {
            _filterClients = value;
        }
        
        [Bindable(event="dashboardDataUpdate")]
        protected function get contactTotal():Number
        {
            var ret:Number = 0;
            for each (var contact:DashboardCountVO in dashboardData.contactCounts)
            ret += contact.count;
            return ret;
        }
        
        [Bindable(event="dashboardDataUpdate")]
        protected function get messageTotal():Number
        {
            var ret:Number = 0;
            for each (var sent:DashboardCountVO in dashboardData.messagesSent)
            {
                ret += sent.count;
            }
            return ret;
        }
        
        [Bindable(event="dashboardDataUpdate")]
        protected function get messageStatistics():ArrayCollection
        {
            var ret:ArrayCollection = new ArrayCollection();
            var keys:Object = new Object();
            
            if (dashboardData != null)
            {
                for each (var sent:DashboardCountVO in dashboardData.messagesSent)
                {
                    if (keys[sent.entryPointType] == null)
                        keys[sent.entryPointType] = new Object();
                    keys[sent.entryPointType].sent = sent.count;
                }
                
                for each (var received:DashboardCountVO in dashboardData.messagesReceived)
                {
                    if (keys[received.entryPointType] == null)
                        keys[received.entryPointType] = new Object();
                    keys[received.entryPointType].received = received.count;
                }
                
                for each (var epType:EntryPointType in currentUser.entryPointTypes)
                {
                    var object:Object = keys[epType.name];
                    
                    if (object == null)
                        object = new Object();
                    
                    if (object.sent == null)
                        object.sent = 0;
                    if (object.received == null)
                        object.received = 0;
                    
                    object.entryPointType = epType.name;
                    object.total = object.sent + object.received;
                    ret.addItem(object);
                }
            }
            return ret;
        }
        
        [Bindable(event="dashboardDataUpdate")]
        protected function getMessageStatistics(entryType:EntryPointType):Object
        {
            for each (var stat:Object in messageStatistics)
            {
                if (stat.entryPointType == entryType.name)
                {
                    return stat;
                }
            }
            return null;
        }
        
        [Bindable(event="dashboardDataUpdate")]
        protected function get clientCredits():ArrayCollection
        {
            var ret:ArrayCollection = new ArrayCollection();
            var keys:Object = new Object();
            
            try
            {
                if (dashboardData != null &&
                    _filterClients != null &&
                    _filterClients.length == 1)
                {
                    var onlyClient:ClientVO = _filterClients.getItemAt(0) as ClientVO;
                    for each (var info:MessageCreditInfoVO in dashboardData.messageCreditInfos)
                    {
                        if (info.clientName == onlyClient.name)
                        {
                            if (keys[info.network] == null)
                                keys[info.network] = new Object();
                            keys[info.network].credits = info.credits;
                        }
                    }
                }
                
                for each (var epType:EntryPointType in currentUser.entryPointTypes)
                {
                    var object:Object = keys[epType.name];
                    
                    if (object == null)
                        object = new Object();
                    
                    if (object.credits == null)
                        object.credits = 0;
                    
                    object.entryPointType = epType.name;
                    ret.addItem(object);
                }
            }
            catch(e:Error)
            {
                ret = new ArrayCollection();
            }
            
            return ret;
        }
        
        [Bindable(event="dashboardDataUpdate")]
        protected function getContactCount(entryType:EntryPointType):DashboardCountVO
        {
            for each (var dc:DashboardCountVO in dashboardData.contactCounts)
            {
                if (dc.entryPointType == entryType.name)
                {
                    return dc;
                }
            }
            return null;
        }
    }
}